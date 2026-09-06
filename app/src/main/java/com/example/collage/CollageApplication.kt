package com.example.collage

import android.app.Application
import com.example.collage.di.appModule
import com.example.collage.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CollageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CollageApplication)
            modules(appModule, databaseModule)
        }
    }
}
