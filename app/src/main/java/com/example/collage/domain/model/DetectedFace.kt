package com.example.collage.domain.model

import android.graphics.Bitmap
import android.graphics.RectF

data class DetectedFace(
    val frameTimeMs: Long,
    val boundingBox: RectF,
    val embedding: FloatArray,
    val frameBitmap: Bitmap,
    val smilingProbability: Float,
    val leftEyeOpenProbability: Float,
    val rightEyeOpenProbability: Float,
    val headEulerAngleX: Float,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float,
    val sharpnessScore: Float,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DetectedFace) return false
        return frameTimeMs == other.frameTimeMs &&
            boundingBox == other.boundingBox &&
            embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = frameTimeMs.hashCode()
        result = 31 * result + boundingBox.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
