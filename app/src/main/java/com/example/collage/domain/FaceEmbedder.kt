package com.example.collage.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

class FaceEmbedder(context: Context) : AutoCloseable {
    private val interpreter: Interpreter

    init {
        val modelFile = context.assets.openFd("mobile_face_net.tflite")
        val inputStream = FileInputStream(modelFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFile.startOffset
        val declaredLength = modelFile.declaredLength
        val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(buffer)
    }

    fun getEmbedding(faceBitmap: Bitmap): FloatArray {
        val resized = resizeBitmap(faceBitmap, 112, 112)
        val inputBuffer = ByteBuffer.allocateDirect(4 * 112 * 112 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 112) {
            for (x in 0 until 112) {
                val pixel = resized.getPixel(x, y)
                // Normalize to [-1, 1] for MobileFaceNet
                inputBuffer.putFloat((((pixel shr 16) and 0xFF) / 255f - 0.5f) / 0.5f)
                inputBuffer.putFloat((((pixel shr 8) and 0xFF) / 255f - 0.5f) / 0.5f)
                inputBuffer.putFloat(((pixel and 0xFF) / 255f - 0.5f) / 0.5f)
            }
        }
        inputBuffer.rewind()

        val outputBuffer = ByteBuffer.allocateDirect(192 * 4)
        outputBuffer.order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val floatBuffer = outputBuffer.asFloatBuffer()
        val output = FloatArray(192)
        floatBuffer.get(output)
        return output
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    override fun close() {
        interpreter.close()
    }
}
