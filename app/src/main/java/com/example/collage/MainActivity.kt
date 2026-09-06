package com.example.collage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.collage.ui.MainScreen
import com.example.collage.ui.CollageViewModel
import com.example.collage.ui.theme.CollageTheme
import com.example.collage.util.FileHelper
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollageTheme {
                val navController = rememberNavController()
                val viewModel: CollageViewModel = koinViewModel()

                MainScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onShare = { bitmap -> FileHelper.shareCollage(this, bitmap) },
                    onSave = { bitmap -> FileHelper.saveToGallery(this, bitmap) }
                )
            }
        }
    }
}
