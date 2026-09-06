package com.example.collage.di

import androidx.room.Room
import com.example.collage.data.local.AppDatabase
import com.example.collage.data.repository.VideoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "collage_database"
        ).build()
    }

    single { get<AppDatabase>().videoDao() }

    singleOf(::VideoRepository)
}
