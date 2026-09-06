package com.example.collage.domain

import android.graphics.Bitmap
import com.example.collage.domain.model.FaceDetectionResult
import com.example.collage.util.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

/**
 * Ported from Snapshot: Wraps ML Kit's on-device face detector.
 * Uses PERFORMANCE_MODE_FAST for responsive on-device processing.
 */
class FaceAnalyzer(private val faceEmbedder: FaceEmbedder) {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setMinFaceSize(0.10f)
            .build()
    )

    suspend fun analyze(bitmap: Bitmap, timeMs: Long): List<FaceDetectionResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val detectedMlFaces = try {
            detector.process(image).await()
        } catch (e: Exception) {
            emptyList()
        }

        val frameFaceResults = mutableListOf<FaceDetectionResult>()
        for (face in detectedMlFaces) {
            val box = face.boundingBox
            if (box.width() < 40 || box.height() < 40) continue

            val crop = ImageUtils.cropFaceForEmbedding(bitmap, box)

            // Calculate composite quality score (Frontality, Sharpness, Eyes, Smile)
            val quality = QualityScorer.calculateQualityScore(
                faceCrop = crop,
                frameWidth = bitmap.width,
                frameHeight = bitmap.height,
                boundingBox = box,
                eulerY = face.headEulerAngleY,
                eulerZ = face.headEulerAngleZ,
                eulerX = face.headEulerAngleX,
                leftEyeOpenProb = face.leftEyeOpenProbability,
                rightEyeOpenProb = face.rightEyeOpenProbability,
                smileProb = face.smilingProbability
            )

            // Extract identity embedding vector
            val embedding = faceEmbedder.extractEmbedding(crop)

            frameFaceResults.add(
                FaceDetectionResult(
                    timestampMs = timeMs,
                    boundingBox = box,
                    headEulerAngleX = face.headEulerAngleX,
                    headEulerAngleY = face.headEulerAngleY,
                    headEulerAngleZ = face.headEulerAngleZ,
                    leftEyeOpenProbability = face.leftEyeOpenProbability,
                    rightEyeOpenProbability = face.rightEyeOpenProbability,
                    smilingProbability = face.smilingProbability,
                    qualityScore = quality,
                    embedding = embedding,
                    frameBitmap = bitmap
                )
            )
        }
        return frameFaceResults
    }

    fun close() = detector.close()
}
