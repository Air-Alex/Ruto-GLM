package com.rosan.ruto.ui.compose

import android.view.Surface
import android.view.SurfaceView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rosan.ruto.ui.viewmodel.TaskExecutionUiState
import com.rosan.ruto.ui.viewmodel.TaskExecutionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TaskExecutionScreen(navController: NavController, insets: WindowInsets) {
    val viewModel: TaskExecutionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var surface by remember { mutableStateOf<Surface?>(null) }

    // Automatically start the task when the surface is available
    LaunchedEffect(surface) {
        val s = surface
        if (s != null && uiState is TaskExecutionUiState.Idle) {
            viewModel.startTask(s)
        }
    }

    // Automatically scroll the log to the bottom
    LaunchedEffect(uiState) {
        if (uiState is TaskExecutionUiState.Running) {
            scrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    // Ensure the task is stopped when the user leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTask()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (uiState is TaskExecutionUiState.Running) {
                FloatingActionButton(onClick = { viewModel.stopTask() }) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Task"
                    )
                }
            }
        },
        contentWindowInsets = insets
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val displayInfo = (uiState as? TaskExecutionUiState.Running)?.displayInfo
            val aspectRatio = if (displayInfo != null && displayInfo.logicalHeight > 0) {
                displayInfo.logicalWidth.toFloat() / displayInfo.logicalHeight.toFloat()
            } else {
                16f / 9f // Default aspect ratio
            }

            // AndroidView to host the SurfaceView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio),
                factory = { context -> SurfaceView(context) },
                update = { view ->
                    if (surface == null) {
                        surface = view.holder.surface
                    }
                }
            )

            // Log display area
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is TaskExecutionUiState.Idle -> {
                        Text("Initializing...")
                    }
                    is TaskExecutionUiState.Running -> {
                        Text(
                            text = state.log,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                    }
                    is TaskExecutionUiState.Success -> {
                        Text(
                            text = state.finalLog,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                    }
                    is TaskExecutionUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                    }
                }
            }
        }
    }
}