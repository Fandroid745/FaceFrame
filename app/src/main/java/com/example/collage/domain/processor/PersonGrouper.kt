package com.example.collage.domain.processor

import com.example.collage.domain.model.AppearanceSegment
import com.example.collage.domain.model.FaceDetectionResult
import com.example.collage.domain.model.PersonProfile
import com.example.collage.util.ImageUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Groups appearance segments into unique person profiles.
 * Enforces chronological grouping and co-occurrence veto.
 */
class PersonGrouper {

    companion object {
        const val PERSON_GROUPING_THRESHOLD = 0.62f
    }

    private class PersonCluster(
        val personId: Int,
        val appearances: MutableList<AppearanceSegment> = mutableListOf(),
        var centroidEmbedding: FloatArray
    ) {
        fun overlapsInTime(segment: AppearanceSegment): Boolean {
            return appearances.any { existing ->
                val overlapStart = max(existing.startTimeMs, segment.startTimeMs)
                val overlapEnd = min(existing.endTimeMs, segment.endTimeMs)
                overlapStart < overlapEnd
            }
        }

        fun addAppearance(segment: AppearanceSegment) {
            appearances.add(segment)
            val n = appearances.size
            for (i in centroidEmbedding.indices) {
                centroidEmbedding[i] = (centroidEmbedding[i] * (n - 1) + segment.representativeEmbedding[i]) / n
            }
            ImageUtils.l2Normalize(centroidEmbedding)
        }
    }

    fun groupAppearances(appearances: List<AppearanceSegment>): List<PersonProfile> {
        if (appearances.isEmpty()) return emptyList()

        val clusters = mutableListOf<PersonCluster>()
        var nextPersonId = 1

        // Chronological sorting is critical for co-occurrence logic
        val sortedAppearances = appearances.sortedBy { it.startTimeMs }

        for (appearance in sortedAppearances) {
            val bestCluster = clusters
                .map { cluster -> cluster to ImageUtils.cosineSimilarity(appearance.representativeEmbedding, cluster.centroidEmbedding) }
                .filter { (cluster, sim) -> sim >= PERSON_GROUPING_THRESHOLD && !cluster.overlapsInTime(appearance) }
                .maxByOrNull { it.second }

            if (bestCluster != null) {
                bestCluster.first.addAppearance(appearance)
            } else {
                clusters.add(
                    PersonCluster(
                        personId = nextPersonId++,
                        appearances = mutableListOf(appearance),
                        centroidEmbedding = appearance.representativeEmbedding.copyOf()
                    )
                )
            }
        }

        // SOLO DETECTION PRIORITY: Favor frames where only this person was on screen.
        // First, count how many faces were in each frame.
        val timestampToFaceCount = mutableMapOf<Long, Int>()
        for (cluster in clusters) {
            for (seg in cluster.appearances) {
                for (det in seg.detections) {
                    timestampToFaceCount[det.timestampMs] = (timestampToFaceCount[det.timestampMs] ?: 0) + 1
                }
            }
        }

        return clusters.map { cluster ->
            val allDetections: List<FaceDetectionResult> = cluster.appearances.flatMap { it.detections }

            // Filter detections where this person appeared alone (no background clutter/mis-detects)
            val soloDetections = allDetections.filter { (timestampToFaceCount[it.timestampMs] ?: 0) == 1 }

            val bestDetection = (if (soloDetections.isNotEmpty()) soloDetections else allDetections)
                .maxByOrNull { it.qualityScore }
                ?: cluster.appearances.first().bestDetection

            // Tight face crop for best isolation (expands 1.7x)
            val representativeShot = ImageUtils.cropGenerousFace(
                source = bestDetection.frameBitmap,
                boundingBox = bestDetection.boundingBox,
                expansionFactor = 1.7f,
                targetAspectRatio = 0.8f
            )

            PersonProfile(
                personId = cluster.personId,
                appearanceCount = cluster.appearances.size,
                appearances = cluster.appearances.sortedBy { it.startTimeMs },
                representativeShot = representativeShot,
                bestDetection = bestDetection,
                averageEmbedding = cluster.centroidEmbedding
            )
        }.sortedBy { it.personId }
    }
}
