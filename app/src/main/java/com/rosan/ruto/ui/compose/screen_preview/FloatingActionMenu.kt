package com.rosan.ruto.ui.compose.screen_preview

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
fun FloatingActionMenu(
    subButtons: List<Pair<ImageVector, String>>,
    onButtonClick: (String) -> Unit,
    onButtonLongClick: (String) -> Unit,
    isButtonEnabled: (String) -> Boolean,
    screenWidth: Float,
    screenHeight: Float,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    val fabAnimatableOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isPlaced by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // 状态控制：是否处于闲置状态
    var isIdle by remember { mutableStateOf(false) }

    val cutout = view.rootWindowInsets?.displayCutout
    val safeLeft = cutout?.safeInsetLeft?.toFloat() ?: 0f
    val safeTop = cutout?.safeInsetTop?.toFloat() ?: 0f
    val safeRight = cutout?.safeInsetRight?.toFloat() ?: 0f
    val safeBottom = cutout?.safeInsetBottom?.toFloat() ?: 0f

    // --- 动画处理 ---

    // 1. 透明度动画
    val idleAlpha by animateFloatAsState(
        targetValue = if (isIdle && !isExpanded) 0.4f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "alphaAnim"
    )

    // 2. 缩进位移动画（解决“突兀”的关键）
    val idleShrinkPx = with(density) { 25.dp.toPx() }
    val currentExtraX by animateFloatAsState(
        targetValue = if (isIdle && !isExpanded) {
            val isAtLeft = fabAnimatableOffset.value.x < screenWidth / 2
            if (isAtLeft) -idleShrinkPx else idleShrinkPx
        } else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offsetAnim"
    )

    // 3. 闲置计时器：3秒不操作进入 idle 状态
    LaunchedEffect(fabAnimatableOffset.value, isExpanded) {
        isIdle = false
        if (!isExpanded) {
            delay(3000)
            isIdle = true
        }
    }

    // 初始化位置
    if (!isPlaced && fabSize != IntSize.Zero) {
        val padding = with(density) { 16.dp.toPx() }
        scope.launch {
            fabAnimatableOffset.snapTo(
                Offset(
                    x = screenWidth - fabSize.width - padding,
                    y = (screenHeight - fabSize.height) / 2f
                )
            )
        }
        isPlaced = true
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (fabAnimatableOffset.value.x + currentExtraX).roundToInt(),
                    fabAnimatableOffset.value.y.roundToInt()
                )
            }
            .onSizeChanged { fabSize = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isExpanded = false
                        isIdle = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val maxX = (screenWidth - fabSize.width - safeRight).coerceAtLeast(0f)
                        val maxY = (screenHeight - fabSize.height - safeBottom).coerceAtLeast(0f)
                        val newX = (fabAnimatableOffset.value.x + dragAmount.x).coerceIn(safeLeft, maxX)
                        val newY = (fabAnimatableOffset.value.y + dragAmount.y).coerceIn(safeTop, maxY)
                        scope.launch { fabAnimatableOffset.snapTo(Offset(newX, newY)) }
                    },
                    onDragEnd = {
                        val padding = with(density) { 16.dp.toPx() }
                        val targetX = if (fabAnimatableOffset.value.x + fabSize.width / 2 < screenWidth / 2) {
                            safeLeft + padding
                        } else {
                            screenWidth - fabSize.width - safeRight - padding
                        }
                        scope.launch {
                            fabAnimatableOffset.animateTo(
                                Offset(targetX, fabAnimatableOffset.value.y),
                                spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val radius = with(density) { 100.dp.toPx() }
        val isAtLeft = fabAnimatableOffset.value.x < screenWidth / 2

        val startAngle = if (isAtLeft) -PI / 2.2 else PI - PI / 2.2
        val endAngle = if (isAtLeft) PI / 2.2 else PI + PI / 2.2
        val step = if (subButtons.size > 1) (endAngle - startAngle) / (subButtons.size - 1) else 0.0

        subButtons.forEachIndexed { index, pair ->
            val angle = (startAngle + index * step).toFloat()
            val progress by animateFloatAsState(
                targetValue = if (isExpanded) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = if (isExpanded) index * 60 else (subButtons.size - 1 - index) * 40
                ), label = "progress_$index"
            )

            val buttonName = pair.second
            // 补回了你原来的参数逻辑
            RadialButton(
                icon = pair.first,
                progress = progress,
                angle = angle,
                radius = radius,
                containerColor = if (isButtonEnabled(buttonName)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                iconTint = if (isButtonEnabled(buttonName)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    onButtonClick(buttonName)
                    isExpanded = false
                },
                onLongClick = { onButtonLongClick(buttonName) }
            )
        }

        val rotationFab by animateFloatAsState(if (isExpanded) 135f else 0f, label = "rotate")

        FloatingActionButton(
            onClick = {
                isExpanded = !isExpanded
                isIdle = false
            },
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    alpha = idleAlpha
                    val scale = if (isIdle && !isExpanded) 0.9f else 1f
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Icon(Icons.Filled.Menu, null, modifier = Modifier.rotate(rotationFab))
        }
    }
}