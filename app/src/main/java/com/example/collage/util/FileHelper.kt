package com.example.collage.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import java.io.OutputStream

object FileHelper {

    fun saveToGallery(context: Context, bitmap: Bitmap) {
        val filename = "Collage_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Collage")
                }
                val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM).toString()
                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            fos?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            } ?: throw Exception("Failed to open output stream")

        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            fos?.close()
        }
    }

    fun shareCollage(context: Context, bitmap: Bitmap) {
        try {
            val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Collage", null)
            val uri = Uri.parse(path)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Collage"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
