package com.example.collage.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.example.collage.domain.model.Person
import kotlin.math.ceil
import kotlin.math.sqrt

class CollageGenerator {

    fun generate(people: List<Person>): Bitmap {
        if (people.isEmpty()) {
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }

        val count = people.size
        val cols = ceil(sqrt(count.toDouble())).toInt()
        val rows = ceil(count.toDouble() / cols).toInt()

        val itemSize = 1000
        val collageWidth = cols * itemSize
        val collageHeight = rows * itemSize

        val collage = Bitmap.createBitmap(collageWidth, collageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        people.forEachIndexed { index, person ->
            val r = index / cols
            val c = index % cols

            val left = c * itemSize
            val top = r * itemSize
            val right = left + itemSize
            val bottom = top + itemSize

            val src = Rect(0, 0, person.representativeBitmap.width, person.representativeBitmap.height)
            val dst = Rect(left, top, right, bottom)

            // Draw the image
            canvas.drawBitmap(person.representativeBitmap, src, dst, paint)

            // Draw appearance count overlay
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 80f
                setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
            }
            canvas.drawText("${person.appearanceCount} apps", left + 40f, bottom - 40f, textPaint)
        }

        return collage
    }
}
