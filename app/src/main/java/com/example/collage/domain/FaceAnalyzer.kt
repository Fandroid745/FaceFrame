package com.example.collage.domain

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.collage.domain.model.FaceAnalysisResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.sqrt

private const val FACE_PADDING_FACTOR = 0.4f

class FaceAnalyzer(private val faceEmbedder: FaceEmbedder) {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build(),
    )

    suspend fun analyze(bitmap: Bitmap): List<FaceAnalysisResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val faces = detector.process(image).await()

        return faces.map { face ->
            val faceCrop = cropFaceGenerously(bitmap, face.boundingBox)
            val sharpness = calculateSharpness(faceCrop)
            val embedding = faceEmbedder.getEmbedding(faceCrop)

            FaceAnalysisResult(
                boundingBox = face.boundingBox,
                faceCropBitmap = faceCrop,
                frameBitmap = bitmap,
                smilingProbability = face.smilingProbability ?: 0f,
                leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f,
                rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f,
                headEulerAngleX = face.headEulerAngleX,
                headEulerAngleY = face.headEulerAngleY,
                headEulerAngleZ = face.headEulerAngleZ,
                sharpnessScore = sharpness,
                embedding = embedding
            )
        }
    }

    fun close() = detector.close()

    private fun cropFaceGenerously(bitmap: Bitmap, box: Rect): Bitmap {
        val padding = (maxOf(box.width(), box.height()) * FACE_PADDING_FACTOR).toInt()
        val left = (box.left - padding).coerceAtLeast(0)
        val top = (box.top - padding).coerceAtLeast(0)
        val right = (box.right + padding).coerceAtMost(bitmap.width)
        val bottom = (box.bottom + padding).coerceAtMost(bitmap.height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    private fun calculateSharpness(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        var sum = 0.0
        var count = 0

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = bitmap.getLuminance(x, y)
                val left = bitmap.getLuminance(x - 1, y)
                val right = bitmap.getLuminance(x + 1, y)
                val top = bitmap.getLuminance(x, y - 1)
                val bottom = bitmap.getLuminance(x, y + 1)
                val laplacian = abs(4 * center - left - right - top - bottom)
                sum += laplacian * laplacian
                count++
            }
        }

        return if (count == 0) 0f else sqrt(sum / count).toFloat()
    }

    private fun Bitmap.getLuminance(x: Int, y: Int): Double {
        val pixel = getPixel(x, y)
        val r = (pixel shr 16 and 0xFF) / 255.0
        val g = (pixel shr 8 and 0xFF) / 255.0
        val b = (pixel and 0xFF) / 255.0
        return 0.299 * r + 0.587 * g + 0.114 * b
    }
}
