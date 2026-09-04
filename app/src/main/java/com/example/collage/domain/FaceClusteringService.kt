package com.example.collage.domain

import com.example.collage.domain.model.Appearance
import com.example.collage.domain.model.DetectedFace
import com.example.collage.domain.model.Person
import com.example.collage.domain.model.VideoResult
import kotlin.math.sqrt

private const val SIMILARITY_THRESHOLD = 0.75f
private const val MAX_GAP_MS = 1000L // 1 second gap allowed for continuous appearance

class FaceClusteringService {

    fun clusterFaces(detectedFaces: List<DetectedFace>): VideoResult {
        val people = mutableListOf<MutableList<DetectedFace>>()

        for (face in detectedFaces) {
            var assigned = false
            for (personFaces in people) {
                if (calculateSimilarity(face.embedding, personFaces.first().embedding) > SIMILARITY_THRESHOLD) {
                    personFaces.add(face)
                    assigned = true
                    break
                }
            }
            if (!assigned) {
                people.add(mutableListOf(face))
            }
        }

        val resultPeople = people.mapIndexed { index, personFaces ->
            val bestShot = personFaces.maxByOrNull { calculateQualityScore(it) }!!
            Person(
                id = index,
                representativeBitmap = bestShot.frameBitmap, // Or cropped face if preferred
                appearanceCount = countAppearances(personFaces),
                bestShotScore = calculateQualityScore(bestShot)
            )
        }

        val appearances = mutableListOf<Appearance>()
        people.forEachIndexed { index, personFaces ->
            appearances.addAll(getAppearanceSegments(index, personFaces))
        }

        return VideoResult(
            people = resultPeople,
            appearances = appearances
        )
    }

    private fun calculateSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        val denom = sqrt(norm1) * sqrt(norm2)
        return if (denom == 0f) 0f else dotProduct / denom
    }

    private fun calculateQualityScore(face: DetectedFace): Float {
        // Higher is better. Factors: sharpness, frontal pose, smiling, eyes open.
        val frontality = 1.0f - (Math.abs(face.headEulerAngleY) / 45f).coerceAtMost(1f)
        return face.sharpnessScore * 0.4f +
               frontality * 0.3f +
               face.smilingProbability * 0.2f +
               ((face.leftEyeOpenProbability + face.rightEyeOpenProbability) / 2f) * 0.1f
    }

    private fun countAppearances(faces: List<DetectedFace>): Int {
        return getAppearanceSegments(-1, faces).size
    }

    private fun getAppearanceSegments(personId: Int, faces: List<DetectedFace>): List<Appearance> {
        if (faces.isEmpty()) return emptyList()
        val sortedFaces = faces.sortedBy { it.frameTimeMs }
        val segments = mutableListOf<Appearance>()

        var startMs = sortedFaces.first().frameTimeMs
        var lastMs = startMs

        for (i in 1 until sortedFaces.size) {
            val currentMs = sortedFaces[i].frameTimeMs
            if (currentMs - lastMs > MAX_GAP_MS) {
                segments.add(Appearance(personId, startMs, lastMs))
                startMs = currentMs
            }
            lastMs = currentMs
        }
        segments.add(Appearance(personId, startMs, lastMs))

        return segments
    }
}
