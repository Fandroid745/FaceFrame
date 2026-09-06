package com.example.collage.domain.processor

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.collage.util.ImageUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object QualityScorer {

    fun calculateQualityScore(
        faceCrop: Bitmap,
        frameWidth: Int,
        frameHeight: Int,
        boundingBox: Rect,
        eulerY: Float,
        eulerZ: Float,
        eulerX: Float,
        leftEyeOpenProb: Float?,
        rightEyeOpenProb: Float?,
        smileProb: Float?
    ): Float {
        // 1. Frontality score (S_front): Euler angles deviation from (0, 0, 0)
        val totalAngleDev = sqrt(eulerY * eulerY + eulerZ * eulerZ + (eulerX * 0.5f) * (eulerX * 0.5f))
        val sFront = max(0f, 1f - min(1f, totalAngleDev / 40f))

        // 2. Sharpness score (S_sharp): Normalized Laplacian variance
        val laplacianVariance = ImageUtils.calculateLaplacianVariance(faceCrop)
        val sSharp = min(1f, max(0f, (laplacianVariance - 50f) / 200f))

        // 3. Eyes open score (S_eyes): Average of both eyes, penalizing blinks
        val avgEyeOpen = if (leftEyeOpenProb != null && rightEyeOpenProb != null) {
            (leftEyeOpenProb + rightEyeOpenProb) / 2f
        } else {
            0.7f
        }
        val sEyes = if (avgEyeOpen < 0.45f) avgEyeOpen * 0.3f else avgEyeOpen

        // 4. Expression / Smile score (S_smile): Slight bonus for pleasant expression
        val sSmile = smileProb ?: 0.3f

        // 5. Border clipping penalty: Prefer full face visible, penalize if cut off at borders
        var borderPenalty = 0f
        val edgeMargin = 12
        if (boundingBox.left <= edgeMargin || boundingBox.top <= edgeMargin ||
            boundingBox.right >= frameWidth - edgeMargin || boundingBox.bottom >= frameHeight - edgeMargin
        ) {
            borderPenalty = 0.35f
        }

        // Weighted composite score (weights sum to 1.0):
        // 35% Frontality + 30% Sharpness + 20% Eyes Open + 15% Smile
        val rawScore = (0.35f * sFront) + (0.30f * sSharp) + (0.20f * sEyes) + (0.15f * sSmile) - borderPenalty

        return max(0.01f, min(1.0f, rawScore))
    }
}
