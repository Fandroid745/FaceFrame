package com.example.collage.util

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Essential image processing utilities for face cropping and sharpness.
 */
object ImageUtils {

    fun cropGenerousFace(
        source: Bitmap,
        boundingBox: Rect,
        expansionFactor: Float = 2.4f,
        targetAspectRatio: Float = 0.8f
    ): Bitmap {
        val centerX = boundingBox.centerX()
        val centerY = boundingBox.centerY()

        val baseSize = max(boundingBox.width(), boundingBox.height()) * expansionFactor

        var targetWidth = baseSize
        var targetHeight = baseSize / targetAspectRatio

        if (targetWidth > source.width) {
            targetWidth = source.width.toFloat()
            targetHeight = targetWidth / targetAspectRatio
        }
        if (targetHeight > source.height) {
            targetHeight = source.height.toFloat()
            targetWidth = targetHeight * targetAspectRatio
        }

        var left = (centerX - targetWidth / 2f).toInt()
        var top = (centerY - targetHeight * 0.42f).toInt() // Slightly offset top for better framing
        var right = (left + targetWidth).toInt()
        var bottom = (top + targetHeight).toInt()

        if (left < 0) {
            right += -left
            left = 0
        }
        if (top < 0) {
            bottom += -top
            top = 0
        }
        if (right > source.width) {
            left = max(0, left - (right - source.width))
            right = source.width
        }
        if (bottom > source.height) {
            top = max(0, top - (bottom - source.height))
            bottom = source.height
        }

        val cropWidth = max(1, right - left)
        val cropHeight = max(1, bottom - top)

        return Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
    }

    fun cropFaceForEmbedding(source: Bitmap, boundingBox: Rect): Bitmap {
        val marginX = (boundingBox.width() * 0.15f).toInt()
        val marginY = (boundingBox.height() * 0.15f).toInt()

        val left = max(0, boundingBox.left - marginX)
        val top = max(0, boundingBox.top - marginY)
        val right = min(source.width, boundingBox.right + marginX)
        val bottom = min(source.height, boundingBox.bottom + marginY)

        val width = max(1, right - left)
        val height = max(1, bottom - top)

        return Bitmap.createBitmap(source, left, top, width, height)
    }

    fun calculateLaplacianVariance(bitmap: Bitmap): Float {
        // Optimized 160x160 downsampling for fast computation
        val scaled = if (bitmap.width > 160 || bitmap.height > 160) {
            Bitmap.createScaledBitmap(bitmap, 160, 160, false)
        } else {
            bitmap
        }

        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = FloatArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            gray[i] = 0.299f * ((p shr 16) and 0xFF) + 0.587f * ((p shr 8) and 0xFF) + 0.114f * (p and 0xFF)
        }

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        for (y in 1 until height - 1) {
            val rowOffset = y * width
            for (x in 1 until width - 1) {
                val laplacian = (
                    gray[rowOffset - width + x] +
                    gray[rowOffset + width + x] +
                    gray[rowOffset + x - 1] +
                    gray[rowOffset + x + 1] -
                    4f * gray[rowOffset + x]
                )
                sum += laplacian
                sumSq += laplacian * laplacian
                count++
            }
        }

        if (count == 0) return 0f
        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return max(0f, variance.toFloat())
    }

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0f
        var dot = 0f
        var n1 = 0f
        var n2 = 0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            n1 += v1[i] * v1[i]
            n2 += v2[i] * v2[i]
        }
        val den = sqrt(n1) * sqrt(n2)
        return if (den > 1e-6f) dot / den else 0f
    }

    fun l2Normalize(v: FloatArray): FloatArray {
        var norm = 0f
        for (x in v) norm += x * x
        val len = sqrt(norm)
        if (len > 1e-6f) {
            for (i in v.indices) v[i] /= len
        }
        return v
    }
}
