package com.rosan.ruto.ui.compose

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.service.KeepAliveService
import com.rosan.ruto.ui.viewmodel.TaskListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPreviewScreen(
    navController: NavController,
    taskKey: String,
) {
    val viewModel: TaskListViewModel = koinViewModel()
    val device: DeviceRepo = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var task = KeepAliveService.instance?.getTasks()[taskKey]

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setSurface(taskKey, null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val view = TextureView(context).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            viewModel.setSurface(taskKey, Surface(surface))
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            viewModel.setSurface(taskKey, null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                        }
                    }
                }
                @SuppressLint("ClickableViewAccessibility")
                view.setOnTouchListener { v, event ->
                    runCatching {
                        viewModel.onTouch(taskKey, event)
                    }
                    true
                }
                return@AndroidView view
            },
            modifier = Modifier.fillMaxSize()
        )

        if (task != null) {
            Text(
                text = """
                    ${task?.status?.name}
                    ${task?.statusMessage}
                """.trimIndent(),
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}
