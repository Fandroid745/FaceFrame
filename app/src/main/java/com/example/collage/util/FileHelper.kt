package com.example.collage.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object FileHelper {

    /**
     * Saves the generated collage bitmap into the device's public Pictures/FaceFrameCollages gallery folder.
     * Uses PNG format.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap) {
        val filename = "Collage_${System.currentTimeMillis()}.png"
        var fos: java.io.OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FaceFrameCollages")
                }
                val imageUri = context.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).toString()
                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            fos?.let {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            } ?: throw Exception("Failed to open output stream")

        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            fos?.close()
        }
    }

    /**
     * Creates an ACTION_SEND Intent with FileProvider content URI to trigger the standard Android Share Sheet.
     * Uses PNG format.
     */
    fun shareCollage(context: Context, bitmap: Bitmap) {
        try {
            val path = android.provider.MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Collage",
                null
            )
            val uri = android.net.Uri.parse(path)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "FaceFrame - Unique People Collage")
                putExtra(android.content.Intent.EXTRA_TEXT, "Generated with FaceFrame on-device AI!")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Collage"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
