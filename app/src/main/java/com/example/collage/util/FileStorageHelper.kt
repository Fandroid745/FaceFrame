package com.example.collage.util

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

object FileStorageHelper {

    suspend fun saveBitmap(context: Context, bitmap: Bitmap, folderName: String, fileNamePrefix: String): String? = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, folderName)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "${fileNamePrefix}_${UUID.randomUUID()}.png"
            val file = File(directory, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
