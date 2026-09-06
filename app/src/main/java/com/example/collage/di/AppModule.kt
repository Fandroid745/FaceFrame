package com.example.collage.di

import com.example.collage.domain.*
import com.example.collage.ui.CollageViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::FaceEmbedder)
    singleOf(::FaceAnalyzer)
    singleOf(::AppearanceTracker)
    singleOf(::PersonGrouper)
    singleOf(::CollageGenerator)
    singleOf(::VideoProcessor)
    viewModelOf(::CollageViewModel)
}
