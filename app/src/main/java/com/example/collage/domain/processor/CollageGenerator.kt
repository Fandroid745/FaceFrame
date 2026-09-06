package com.example.collage.domain.processor

import android.graphics.*
import com.example.collage.domain.model.PersonProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Ported from Snapshot: Renders a professional 9:16 (1080x1920) collage.
 * Uses dynamic Instagram Story layouts for various person counts.
 */
class CollageGenerator {
    companion object {
        const val CANVAS_WIDTH = 1080
        const val CANVAS_HEIGHT = 1920
    }

    suspend fun generate(people: List<PersonProfile>): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Dark gradient background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, CANVAS_HEIGHT.toFloat(),
                intArrayOf(
                    Color.parseColor("#0F172A"), // Deep slate
                    Color.parseColor("#18182E"), // Indigo-slate
                    Color.parseColor("#090D16")  // Dark midnight
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CANVAS_WIDTH.toFloat(), CANVAS_HEIGHT.toFloat(), bgPaint)

        // 2. Content Area
        val contentTop = 100f
        val contentBottom = CANVAS_HEIGHT - 40f
        val contentLeft = 32f
        val contentRight = CANVAS_WIDTH - 32f
        val contentWidth = contentRight - contentLeft
        val contentHeight = contentBottom - contentTop

        val tileRects = calculateLayout(people.size, contentLeft, contentTop, contentWidth, contentHeight)

        for (i in people.indices) {
            if (i >= tileRects.size) break
            drawPersonTile(canvas, tileRects[i], people[i])
        }

        bitmap
    }

    private fun calculateLayout(
        count: Int,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ): List<RectF> {
        val rects = mutableListOf<RectF>()
        val gap = 18f

        when (count) {
            0 -> Unit
            1 -> {
                rects.add(RectF(left, top, left + width, top + height))
            }
            2 -> {
                val h = (height - gap) / 2f
                rects.add(RectF(left, top, left + width, top + h))
                rects.add(RectF(left, top + h + gap, left + width, top + 2 * h + gap))
            }
            3 -> {
                val topH = (height - gap) * 0.52f
                val botH = height - topH - gap
                val botW = (width - gap) / 2f

                rects.add(RectF(left, top, left + width, top + topH))
                rects.add(RectF(left, top + topH + gap, left + botW, top + height))
                rects.add(RectF(left + botW + gap, top + topH + gap, left + width, top + height))
            }
            4 -> {
                val colW = (width - gap) / 2f
                val rowH = (height - gap) / 2f
                for (r in 0..1) for (c in 0..1) {
                    val cardL = left + c * (colW + gap)
                    val cardT = top + r * (rowH + gap)
                    rects.add(RectF(cardL, cardT, cardL + colW, cardT + rowH))
                }
            }
            5 -> {
                val row1H = (height - gap) * 0.52f
                val row2H = height - row1H - gap
                val topColW = (width - gap) / 2f
                rects.add(RectF(left, top, left + topColW, top + row1H))
                rects.add(RectF(left + topColW + gap, top, left + width, top + row1H))

                val botColW = (width - 2 * gap) / 3f
                val botT = top + row1H + gap
                for (c in 0..2) {
                    val cardL = left + c * (botColW + gap)
                    rects.add(RectF(cardL, botT, cardL + botColW, botT + row2H))
                }
            }
            else -> {
                val rows = (count + 1) / 2
                val colW = (width - gap) / 2f
                val rowH = (height - (rows - 1) * gap) / rows
                for (i in 0 until count) {
                    val r = i / 2
                    val c = i % 2
                    val cardL = left + c * (colW + gap)
                    val cardT = top + r * (rowH + gap)
                    rects.add(RectF(cardL, cardT, cardL + colW, cardT + rowH))
                }
            }
        }
        return rects
    }

    private fun drawPersonTile(canvas: Canvas, rect: RectF, person: PersonProfile) {
        val cornerRadius = 24f

        val clipPath = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }

        canvas.save()
        canvas.clipPath(clipPath)

        val bmp = person.representativeShot
        val src = calculateCenterCropRect(bmp.width, bmp.height, rect.width().toInt(), rect.height().toInt())
        canvas.drawBitmap(bmp, src, rect, Paint(Paint.FILTER_BITMAP_FLAG))

        canvas.restore()

        // Card border (Snapshot subtle white style)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }

    private fun calculateCenterCropRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcRatio = srcW.toFloat() / srcH
        val dstRatio = dstW.toFloat() / dstH
        var cropW = srcW
        var cropH = srcH
        var cropX = 0
        var cropY = 0
        if (srcRatio > dstRatio) {
            cropW = (srcH * dstRatio).toInt()
            cropX = (srcW - cropW) / 2
        } else {
            cropH = (srcW / dstRatio).toInt()
            cropY = ((srcH - cropH) * 0.35f).toInt() // Top-bias
        }
        return Rect(cropX, cropY, cropX + cropW, cropY + cropH)
    }
}
