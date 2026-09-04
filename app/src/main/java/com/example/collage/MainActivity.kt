package com.example.collage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.collage.domain.*
import com.example.collage.ui.CollageScreen
import com.example.collage.ui.CollageViewModel
import com.example.collage.ui.theme.CollageTheme
import com.example.collage.util.FileHelper

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CollageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual DI for simplicity in assignment
        val faceEmbedder = FaceEmbedder(this)
        val faceAnalyzer = FaceAnalyzer(faceEmbedder)
        val faceClusteringService = FaceClusteringService()
        val videoProcessor = VideoProcessor(this, faceAnalyzer, faceClusteringService)
        val collageGenerator = CollageGenerator()

        viewModel = CollageViewModel(videoProcessor, collageGenerator)

        enableEdgeToEdge()
        setContent {
            CollageTheme {
                CollageAppWrapper(viewModel)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CollageAppWrapper(viewModel: CollageViewModel) {
        CollageScreen(
            viewModel = viewModel,
            onShare = { bitmap -> FileHelper.shareCollage(this, bitmap) },
            onSave = { bitmap -> FileHelper.saveToGallery(this, bitmap) }
        )
    }
}
