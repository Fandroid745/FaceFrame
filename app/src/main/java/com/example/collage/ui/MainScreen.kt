package com.example.collage.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.collage.R
import com.example.collage.ui.components.CollageDetailContent
import com.example.collage.ui.history.HistoryScreen
import com.example.collage.ui.home.HomePickerContent

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
                "home" -> stringResource(R.string.app_name)
                "history" -> stringResource(R.string.title_history)
                "history_detail" -> stringResource(R.string.title_saved_collage)
                else -> stringResource(R.string.app_name)
            }
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (currentDestination == "history_detail") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
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
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_history)) },
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
                    is UiState.Idle, is UiState.Error, is UiState.Processing -> {
                        HomePickerContent(
                            uiState = state,
                            onVideoSelected = { viewModel.processVideo(it) },
                            onRecentClick = { video ->
                                viewModel.loadHistoryDetail(video)
                                navController.navigate("history_detail")
                            },
                            onCancelProcessing = { viewModel.cancelProcessing() },
                            recentCollage = history.firstOrNull()
                        )
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
