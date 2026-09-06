package com.example.collage.domain.processor

import com.example.collage.domain.model.AppearanceSegment
import com.example.collage.domain.model.FaceDetectionResult
import com.example.collage.util.ImageUtils

/**
 * Tracks face appearances chronologically across video frames.
 * Groups contiguous face detections into continuous appearance segments.
 * Ported from Snapshot: uses Global Best-First Assignment for multi-face robustness.
 */
class AppearanceTracker {

    companion object {
        const val TRACKING_SIMILARITY_THRESHOLD = 0.60f
        const val MAX_TRACKING_GAP_MS = 350L
        const val MIN_APPEARANCE_DURATION_MS = 250L
    }

    private class ActiveTrack(
        val startTimeMs: Long,
        var lastSeenTimeMs: Long,
        val detections: MutableList<FaceDetectionResult> = mutableListOf(),
        var runningEmbedding: FloatArray
    )

    private val activeTracks = mutableListOf<ActiveTrack>()
    private val completedSegments = mutableListOf<AppearanceSegment>()

    fun reset() {
        activeTracks.clear()
        completedSegments.clear()
    }

    fun processFrameDetections(timestampMs: Long, faces: List<FaceDetectionResult>) {
        // 1. Finalize inactive tracks (gap > 350ms)
        val iterator = activeTracks.iterator()
        while (iterator.hasNext()) {
            val track = iterator.next()
            if (timestampMs - track.lastSeenTimeMs > MAX_TRACKING_GAP_MS) {
                finalizeTrack(track)
                iterator.remove()
            }
        }

        val unmatchedFaces = faces.toMutableList()

        // 2. Global Best-First Assignment
        val assignments = mutableListOf<Triple<ActiveTrack, FaceDetectionResult, Float>>()
        for (track in activeTracks) {
            for (face in unmatchedFaces) {
                val sim = ImageUtils.cosineSimilarity(face.embedding, track.runningEmbedding)
                if (sim >= TRACKING_SIMILARITY_THRESHOLD) {
                    assignments.add(Triple(track, face, sim))
                }
            }
        }
        assignments.sortByDescending { it.third }

        val assignedTracks = mutableSetOf<ActiveTrack>()
        val assignedFaces = mutableSetOf<FaceDetectionResult>()
        for ((track, face, _) in assignments) {
            if (track in assignedTracks || face in assignedFaces) continue
            track.detections.add(face)
            track.lastSeenTimeMs = timestampMs
            updateRunningEmbedding(track, face.embedding)
            unmatchedFaces.remove(face)
            assignedTracks.add(track)
            assignedFaces.add(face)
        }

        // 3. New tracks for unmatched faces
        for (newFace in unmatchedFaces) {
            activeTracks.add(
                ActiveTrack(
                    startTimeMs = timestampMs,
                    lastSeenTimeMs = timestampMs,
                    detections = mutableListOf(newFace),
                    runningEmbedding = newFace.embedding.copyOf()
                )
            )
        }
    }

    fun finishTracking(): List<AppearanceSegment> {
        for (track in activeTracks) {
            finalizeTrack(track)
        }
        activeTracks.clear()
        return completedSegments.filter { it.durationMs >= MIN_APPEARANCE_DURATION_MS || it.detections.size >= 2 }
    }

    private fun finalizeTrack(track: ActiveTrack) {
        if (track.detections.isEmpty()) return
        val bestDetection = track.detections.maxByOrNull { it.qualityScore } ?: track.detections.first()

        // Representative embedding: average of top 3 quality detections
        val topDetections = track.detections.sortedByDescending { it.qualityScore }.take(3)
        val repEmbedding = FloatArray(topDetections.first().embedding.size)
        for (det in topDetections) {
            for (i in repEmbedding.indices) {
                repEmbedding[i] += det.embedding[i]
            }
        }
        ImageUtils.l2Normalize(repEmbedding)

        completedSegments.add(
            AppearanceSegment(
                startTimeMs = track.startTimeMs,
                endTimeMs = track.lastSeenTimeMs,
                detections = track.detections.toList(),
                representativeEmbedding = repEmbedding,
                bestDetection = bestDetection
            )
        )
    }

    private fun updateRunningEmbedding(track: ActiveTrack, newEmbedding: FloatArray) {
        val n = track.detections.size
        for (i in track.runningEmbedding.indices) {
            track.runningEmbedding[i] = (track.runningEmbedding[i] * (n - 1) + newEmbedding[i]) / n
        }
        ImageUtils.l2Normalize(track.runningEmbedding)
    }
}
