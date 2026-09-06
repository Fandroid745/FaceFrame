package com.example.collage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.collage.data.local.daos.VideoDao
import com.example.collage.data.local.entity.PersonRecord
import com.example.collage.data.local.entity.VideoRecord

@Database(entities = [VideoRecord::class, PersonRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
