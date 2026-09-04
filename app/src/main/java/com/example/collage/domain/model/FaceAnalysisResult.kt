package com.example.collage.domain.model

import android.graphics.Bitmap
import android.graphics.Rect

data class FaceAnalysisResult(
    val boundingBox: Rect,
    val faceCropBitmap: Bitmap,
    val frameBitmap: Bitmap,
    val smilingProbability: Float,
    val leftEyeOpenProbability: Float,
    val rightEyeOpenProbability: Float,
    val headEulerAngleX: Float,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float,
    val sharpnessScore: Float,
    val embedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceAnalysisResult) return false
        if (boundingBox != other.boundingBox) return false
        if (!embedding.contentEquals(other.embedding)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = boundingBox.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
