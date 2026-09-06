package com.example.collage.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.collage.ui.screens.CollageDetailContent
import com.example.collage.ui.screens.HistoryScreen
import com.example.collage.ui.screens.HomePickerContent
import com.example.collage.ui.screens.HomeProcessingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CollageViewModel,
    navController: NavHostController,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val title = when (currentDestination) {
                "home" -> "New Collage"
                "history" -> "History"
                "history_detail" -> "Saved Collage"
                else -> "Face Collage"
            }
            TopAppBar(title = { Text(title) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentDestination == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
                    label = { Text("History") },
                    selected = currentDestination == "history" || currentDestination == "history_detail",
                    onClick = {
                        navController.navigate("history") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
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
                when (val state = uiState) {
                    is UiState.Idle, is UiState.Error -> {
                        HomePickerContent(
                            onVideoSelected = { viewModel.processVideo(it) },
                            isIdle = true
                        )
                    }
                    is UiState.Processing -> {
                        HomeProcessingContent(state)
                    }
                    is UiState.Success -> {
                        CollageDetailContent(
                            uiState = state,
                            onShare = onShare,
                            onSave = onSave,
                            onReset = { viewModel.reset() }
                        )
                    }
                    is UiState.SavedSuccess -> {
                        // If we are in SavedSuccess but on Home tab, it means we probably just navigated from history?
                        // Actually, we usually want Home to stay on its own state.
                        // For simplicity, let's allow it to show.
                        CollageDetailContent(
                            uiState = state,
                            onShare = onShare,
                            onSave = onSave,
                            onReset = { viewModel.reset() }
                        )
                    }
                }
            }
            composable("history") {
                HistoryScreen(
                    history = history,
                    onItemClick = { video ->
                        viewModel.loadHistoryDetail(video)
                        navController.navigate("history_detail")
                    },
                    onDeleteClick = { viewModel.deleteHistoryItem(it) }
                )
            }
            composable("history_detail") {
                CollageDetailContent(
                    uiState = uiState,
                    onShare = onShare,
                    onSave = onSave,
                    onReset = {
                        viewModel.reset()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
