package com.example.collage.domain

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.channels.FileChannel

class FaceEmbedder(context: Context) {
    private val interpreter: Interpreter
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f)) // Normalize to [-1, 1]
        .build()

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
        val tensorImage = TensorImage.fromBitmap(faceBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val output = Array(1) { FloatArray(192) } // MobileFaceNet output is often 192 or 128

        interpreter.run(processedImage.buffer, output)
        return output[0]
    }

    fun close() {
        interpreter.close()
    }
}
