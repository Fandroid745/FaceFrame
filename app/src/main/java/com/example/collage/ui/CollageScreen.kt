package com.example.collage.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.collage.ui.screens.HomeScreen
import com.example.collage.ui.screens.ProcessingScreen
import com.example.collage.ui.screens.ResultScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollageScreen(
    viewModel: CollageViewModel,
    navController: NavHostController,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Automatically navigate based on state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Processing -> {
                if (currentDestination != "processing") {
                    navController.navigate("processing") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            }
            is UiState.Success -> {
                if (currentDestination != "result") {
                    navController.navigate("result") {
                        popUpTo("processing") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is UiState.Idle -> {
                if (currentDestination != "home") {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Face Collage") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentDestination == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    selected = currentDestination == "processing",
                    onClick = {
                        navController.navigate("processing") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Collage") },
                    label = { Text("Collage") },
                    selected = currentDestination == "result",
                    onClick = {
                        navController.navigate("result") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onVideoSelected = { viewModel.processVideo(it) },
                    isIdle = uiState is UiState.Idle
                )
            }
            composable("processing") {
                ProcessingScreen(uiState)
            }
            composable("result") {
                ResultScreen(
                    uiState = uiState,
                    onShare = onShare,
                    onSave = onSave,
                    onReset = { viewModel.reset() }
                )
            }
        }
    }
}
