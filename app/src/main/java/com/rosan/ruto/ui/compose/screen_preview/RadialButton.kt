package com.rosan.ruto.ui.compose.screen_preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadialButton(
    icon: ImageVector,
    progress: Float,
    angle: Float,
    radius: Float,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // 计算偏移量
    val xOffset = (cos(angle.toDouble()) * radius * progress).toFloat()
    val yOffset = (sin(angle.toDouble()) * radius * progress).toFloat()

    Surface(
        modifier = Modifier
            .offset { IntOffset(xOffset.roundToInt(), yOffset.roundToInt()) }
            .size(40.dp) // 子按钮通常比主 FAB 小一点
            .alpha(progress) // 随进度淡入淡出
            .scale(progress), // 随进度缩放
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp
    ) {
        // 使用 Box 包装，确保 clickable 应用在 Surface 内部的最上层
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    // 确保水波纹是有边界的圆
                    interactionSource = remember { MutableInteractionSource() },
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}