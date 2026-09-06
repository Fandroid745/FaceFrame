package com.example.collage.domain.model

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Result of detecting and embedding a face in a single frame.
 */
data class FaceDetectionResult(
    val timestampMs: Long,
    val boundingBox: Rect,
    val headEulerAngleX: Float,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val smilingProbability: Float?,
    val qualityScore: Float,
    val embedding: FloatArray,
    val frameBitmap: Bitmap
)

/**
 * A continuous appearance segment of a single tracked face.
 */
data class AppearanceSegment(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val detections: List<FaceDetectionResult>,
    val representativeEmbedding: FloatArray,
    val bestDetection: FaceDetectionResult
) {
    val durationMs: Long get() = endTimeMs - startTimeMs
}

/**
 * Final identity profile grouped from one or more appearance segments.
 */
data class PersonProfile(
    val personId: Int,
    val appearanceCount: Int,
    val appearances: List<AppearanceSegment>,
    val representativeShot: Bitmap,
    val bestDetection: FaceDetectionResult,
    val averageEmbedding: FloatArray
)

/**
 * Container for the final video analysis results.
 */
data class VideoAnalysisResult(
    val videoUri: String,
    val durationMs: Long,
    val totalAppearances: Int,
    val personCount: Int,
    val persons: List<PersonProfile>,
    val collageBitmap: Bitmap
)
