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
)
