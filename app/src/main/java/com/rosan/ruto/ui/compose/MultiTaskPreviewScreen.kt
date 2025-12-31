package com.rosan.ruto.ui.compose

import android.app.Activity
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rosan.ruto.ui.viewmodel.MultiTaskPreviewViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.*

@Composable
fun MultiTaskPreviewScreen(navController: NavController, displayIds: List<Int>) {
    val viewModel: MultiTaskPreviewViewModel = koinViewModel { parametersOf(displayIds) }
    val displaySizes by viewModel.displaySizes.collectAsState()
    val view = LocalView.current
    val density = LocalDensity.current

    val zIndexMap = remember {
        mutableStateMapOf<Int, Float>().apply {
            displayIds.forEachIndexed { index, id -> put(id, index.toFloat()) }
        }
    }
    var topZIndex by remember { mutableFloatStateOf(displayIds.size.toFloat()) }

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose { insetsController?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C1E))
            .drawBehind {
                val step = 40.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    drawLine(
                        Color.White.copy(alpha = 0.1f),
                        Offset(x.toFloat(), 0f),
                        Offset(x.toFloat(), size.height),
                        1f
                    )
                }
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(
                        Color.White.copy(alpha = 0.1f),
                        Offset(0f, y.toFloat()),
                        Offset(size.width, y.toFloat()),
                        1f
                    )
                }
            }
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        displayIds.forEachIndexed { index, displayId ->
            val remoteDisplaySize = displaySizes[displayId]
            if (remoteDisplaySize != null && remoteDisplaySize.width > 0 && remoteDisplaySize.height > 0) {

                val (windowWidthDp, windowHeightDp) = remember(
                    remoteDisplaySize,
                    maxWidth,
                    maxHeight
                ) {
                    val remoteAspect = remoteDisplaySize.width / remoteDisplaySize.height
                    val screenAspect = maxWidth.value / maxHeight.value
                    if (remoteAspect > screenAspect) {
                        Pair(maxWidth, maxWidth / remoteAspect)
                    } else {
                        Pair(maxHeight * remoteAspect, maxHeight)
                    }
                }

                // 初始位置：居中向右
                val initialOffset = remember(displayId, screenWidthPx, screenHeightPx) {
                    val wPx = with(density) { windowWidthDp.toPx() }
                    val hPx = with(density) { windowHeightDp.toPx() }
                    val centerX = (screenWidthPx - wPx) / 2f
                    val centerY = (screenHeightPx - hPx) / 2f
                    val xShift = with(density) { index * 60.dp.toPx() }
                    Offset(centerX + xShift, centerY)
                }

                TaskWindow(
                    displayId = displayId,
                    zIndex = zIndexMap[displayId] ?: 0f,
                    initialOffset = initialOffset,
                    width = windowWidthDp,
                    height = windowHeightDp,
                    remoteSize = remoteDisplaySize,
                    viewModel = viewModel,
                    onInteract = {
                        if (zIndexMap[displayId] != topZIndex) {
                            topZIndex += 1f
                            zIndexMap[displayId] = topZIndex
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskWindow(
    displayId: Int,
    zIndex: Float,
    initialOffset: Offset,
    width: Dp,
    height: Dp,
    remoteSize: Size,
    viewModel: MultiTaskPreviewViewModel,
    onInteract: () -> Unit
) {
    // 1. 布局位移 (控制盒子在屏幕上的位置)
    var offset by remember(displayId) { mutableStateOf(initialOffset) }
    // 2. 绘制变换 (只负责缩放和旋转)
    var scale by remember(displayId) { mutableFloatStateOf(1f) }
    var rotation by remember(displayId) { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .zIndex(zIndex)
            // 第一步：应用位移。此时 offset 对应的是屏幕 1:1 的像素。
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            // 第二步：应用缩放和旋转。graphicsLayer 会以当前 Box 中心为原点进行变换。
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationZ = rotation
            }
            .size(width, height)
            .background(Color.Black)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            .pointerInput(displayId) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    onInteract()

                    // 【致命点修正】：
                    // detectTransformGestures 的 pan 是基于 Box 的本地坐标系的。
                    // 当 Box 被缩放后，本地坐标系的 1px 不再等于屏幕的 1px。
                    // 为了让手感 1:1，必须把 pan 映射回屏幕缩放后的比例。

                    // 1. 抵消旋转：
                    val angleRad = rotation * (PI.toFloat() / 180f)
                    val cosA = cos(angleRad)
                    val sinA = sin(angleRad)
                    val rotatedPanX = pan.x * cosA - pan.y * sinA
                    val rotatedPanY = pan.x * sinA + pan.y * cosA

                    // 2. 【核心补偿】：乘以当前的 scale。
                    // 放大 10 倍时，Box 变大了，手指动 1px，offset 必须加 10px 才能视觉跟手。
                    // 缩小 0.1 倍时，Box 变小了，手指动 1px，offset 只加 0.1px 才能防止飞出去。
                    offset += Offset(rotatedPanX * scale, rotatedPanY * scale)

                    scale = (scale * zoom).coerceIn(0.05f, 15f)
                    rotation += rotationChange
                }
            }
            .pointerInput(displayId) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            onInteract()
                        }
                    }
                }
            }
    ) {
        // ... AndroidView 保持不变 ...
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    isOpaque = true
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                            st.setDefaultBufferSize(
                                remoteSize.width.roundToInt(),
                                remoteSize.height.roundToInt()
                            )
                            viewModel.setSurface(displayId, Surface(st))
                        }

                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            viewModel.setSurface(displayId, null)
                            return true
                        }

                        override fun onSurfaceTextureSizeChanged(
                            st: SurfaceTexture,
                            w: Int,
                            h: Int
                        ) {
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}