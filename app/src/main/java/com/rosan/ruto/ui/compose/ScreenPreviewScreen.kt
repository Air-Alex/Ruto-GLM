package com.rosan.ruto.ui.compose

import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.rosan.ruto.ui.compose.screen_preview.AppPickerDialog
import com.rosan.ruto.ui.compose.screen_preview.FloatingActionMenu
import com.rosan.ruto.ui.viewmodel.ScreenPreviewViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenPreviewScreen(navController: NavController, insets: WindowInsets, displayId: Int) {
    val viewModel: ScreenPreviewViewModel = koinViewModel { parametersOf(displayId) }
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // --- 状态定义 ---
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var isTouchEnabled by remember { mutableStateOf(true) }
    var isTransformEnabled by remember { mutableStateOf(false) }
    var showAppPickerDialog by remember { mutableStateOf(false) }

    // --- 关键：持有 TextureView 的引用 ---
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

    // --- 辅助函数：统一更新矩阵 ---
    fun updateMatrix() {
        val textureView = textureViewRef ?: return
        val vw = textureView.width.toFloat()
        val vh = textureView.height.toFloat()

        // 从 viewModel 获取实际画面的尺寸
        val rw = viewModel.displaySize.width
        val rh = viewModel.displaySize.height

        if (vw > 0f && vh > 0f && rw > 0f && rh > 0f) {
            val matrix = Matrix().apply {
                // --- 第一步：计算基础适配缩放 (Center Inside) ---
                val scaleX = vw / rw
                val scaleY = vh / rh
                val baseScale = minOf(scaleX, scaleY)

                // --- 第二步：应用变换 ---
                // 1. 先缩放到适应屏幕的大小
                postScale(baseScale, baseScale, rw / 2f, rh / 2f)

                // 2. 将画面中心移动到 View 的中心
                postTranslate((vw - rw) / 2f, (vh - rh) / 2f)

                // 3. 应用用户的手势变换 (以 View 中心为原点)
                postScale(scale, scale, vw / 2f, vh / 2f)
                postRotate(rotation, vw / 2f, vh / 2f)
                postTranslate(offset.x, offset.y)
            }
            textureView.setTransform(matrix)
        }
    }

    // 沉浸式状态栏
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose { insetsController?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    fun Modifier.drawGrid(color: Color): Modifier = this.drawBehind {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(
                color,
                Offset(x.toFloat(), 0f),
                Offset(x.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(
                color,
                Offset(0f, y.toFloat()),
                Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C1E)) // 深色背景
            .drawGrid(Color.White.copy(alpha = 0.25f))
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val center = Offset(screenWidth / 2f, screenHeight / 2f)

        // 1. 底层：AndroidView (只负责显示和单点触控)
        AndroidView(factory = { context ->
            TextureView(context).apply {
                isOpaque = false
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(s: SurfaceTexture, w: Int, h: Int) {
                        viewModel.setSurface(Surface(s))
                        textureViewRef = this@apply
                        post { updateMatrix() }
                    }

                    override fun onSurfaceTextureSizeChanged(
                        s: SurfaceTexture, w: Int, h: Int
                    ) {
                        updateMatrix()
                    }

                    override fun onSurfaceTextureDestroyed(s: SurfaceTexture): Boolean {
                        viewModel.setSurface(null)
                        textureViewRef = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(s: SurfaceTexture) {}
                }

                fun transformMotionEvent(event: MotionEvent, inverseMatrix: Matrix): MotionEvent {
                    val pointerCount = event.pointerCount
                    val pointerProperties =
                        arrayOfNulls<MotionEvent.PointerProperties>(pointerCount)
                    val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(pointerCount)

                    for (i in 0 until pointerCount) {
                        // 获取原始属性
                        val prop = MotionEvent.PointerProperties()
                        event.getPointerProperties(i, prop)
                        pointerProperties[i] = prop

                        // 获取原始坐标并应用逆矩阵
                        val coords = MotionEvent.PointerCoords()
                        event.getPointerCoords(i, coords)

                        val pts = floatArrayOf(coords.x, coords.y)
                        inverseMatrix.mapPoints(pts)

                        coords.x = pts[0]
                        coords.y = pts[1]
                        pointerCoords[i] = coords
                    }

                    return MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        event.action,
                        pointerCount,
                        pointerProperties.requireNoNulls(),
                        pointerCoords.requireNoNulls(),
                        event.metaState,
                        event.buttonState,
                        event.xPrecision,
                        event.yPrecision,
                        event.deviceId,
                        event.edgeFlags,
                        event.source,
                        event.flags
                    )
                }

                setOnTouchListener { v, event ->
                    if (isTransformEnabled) return@setOnTouchListener false

                    if (isTouchEnabled) {
                        val matrix = Matrix()
                        (v as TextureView).getTransform(matrix) // 获取当前的缩放/位移/旋转矩阵

                        val inverse = Matrix()
                        if (matrix.invert(inverse)) {
                            // 使用我们修正后的多指转换函数
                            val transformedEvent = transformMotionEvent(event, inverse)

                            // 注入到 ViewModel
                            viewModel.injectEvent(transformedEvent)
                            transformedEvent.recycle()
                        }
                        true
                    } else false
                }
            }
        }, modifier = Modifier.fillMaxSize(), update = { textureView ->
            textureViewRef = textureView
            textureView.isEnabled = !isTransformEnabled
            updateMatrix()
        })

        if (isTransformEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rot ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(0.1f, 10f)

                            scale = newScale
                            rotation += rot

                            val zoomChange = newScale / oldScale
                            if (zoomChange != 1f && zoomChange.isFinite()) {
                                val moveNode = centroid - center - offset
                                offset += moveNode * (1 - 1 / zoomChange)
                            }
                            offset += pan
                            updateMatrix()
                        }
                    })
        }

        if (showAppPickerDialog) {
            AppPickerDialog(
                onDismiss = { showAppPickerDialog = false },
                onAppSelected = { packageName ->
                    showAppPickerDialog = false
                    viewModel.launch(packageName)
                }
            )
        }

        FloatingActionMenu(
            subButtons = listOf(
                Icons.AutoMirrored.Filled.ArrowBack to "Back",
                Icons.Filled.Lock to "Lock",
                (if (isTouchEnabled) Icons.Filled.TouchApp else Icons.Filled.FrontHand) to "Touch",
                Icons.Filled.Apps to "Select App",
//                Icons.Filled.Tune to "Settings",
                Icons.Filled.Close to "Close"
            ),
            onButtonClick = {
                when (it) {
                    "Back" -> viewModel.clickBack()
                    "Lock" -> {
                        isTransformEnabled = false
                        isTouchEnabled = false
                    }

                    "Touch" -> {
                        isTouchEnabled = !isTouchEnabled
                        isTransformEnabled = !isTouchEnabled
                        updateMatrix()
                    }

                    "Select App" -> showAppPickerDialog = true
                    "Close" -> navController.popBackStack()
                }
            },
            onButtonLongClick = {
                if (it == "Lock") {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    val animScale = Animatable(scale).apply { updateBounds(0.1f, 10f) }
                    val animRot = Animatable(rotation)
                    val animOff = Animatable(offset, Offset.VectorConverter)

                    scope.launch {
                        launch { animScale.animateTo(1f); scale = 1f }
                        launch { animRot.animateTo(0f); rotation = 0f }
                        launch { animOff.animateTo(Offset.Zero); offset = Offset.Zero }
                    }
                    scope.launch {
                        while (animScale.isRunning || animRot.isRunning || animOff.isRunning) {
                            scale = animScale.value
                            rotation = animRot.value
                            offset = animOff.value
                            updateMatrix()
                            withFrameNanos { }
                        }
                        updateMatrix()
                    }
                }
            },
            isButtonEnabled = {
                (it == "Lock" && !isTransformEnabled && !isTouchEnabled) || (it == "Touch" && (isTouchEnabled || isTransformEnabled))
            },
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}
