package com.example.collage.domain.model

data class VideoResult(
    val people: List<Person>,
    val appearances: List<Appearance>,
    val totalAppearances: Int = appearances.size,
)
