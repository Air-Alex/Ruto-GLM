package com.rosan.ruto.ui.compose.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection)
                + paddingValues.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + paddingValues.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection)
                + paddingValues.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding()
    )
}

@Composable
fun PaddingValues.exclude(
    sides: WindowInsetsSides,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    density: Density = LocalDensity.current
) = asWindowInsets(layoutDirection).exclude(sides).asPaddingValues(density)

@Composable
fun PaddingValues.only(
    sides: WindowInsetsSides,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
    density: Density = LocalDensity.current
) = asWindowInsets(layoutDirection).only(sides).asPaddingValues(density)

@Composable
fun PaddingValues.asWindowInsets(): WindowInsets {
    return asWindowInsets(LocalLayoutDirection.current)
}

fun PaddingValues.asWindowInsets(layoutDirection: LayoutDirection) =
    WindowInsets(
        left = calculateLeftPadding(layoutDirection),
        top = calculateTopPadding(),
        right = calculateRightPadding(layoutDirection),
        bottom = calculateBottomPadding()
    )

@Composable
fun PaddingValues.calculateLeftPadding() =
    calculateLeftPadding(LocalLayoutDirection.current)

@Composable
fun PaddingValues.calculateRightPadding() =
    calculateRightPadding(LocalLayoutDirection.current)

@Composable
fun PaddingValues.calculateStartPadding() =
    calculateStartPadding(LocalLayoutDirection.current)

@Composable
fun PaddingValues.calculateEndPadding() =
    calculateEndPadding(LocalLayoutDirection.current)
