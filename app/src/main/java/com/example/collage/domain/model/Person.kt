package com.example.collage.domain.model

import android.graphics.Bitmap

data class Person(
    val id: Int,
    val representativeBitmap: Bitmap,
    val appearanceCount: Int,
    val bestShotScore: Float,
)
