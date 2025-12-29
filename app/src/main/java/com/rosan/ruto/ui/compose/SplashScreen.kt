package com.rosan.ruto.ui.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.ruto.ui.Destinations
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, insets: PaddingValues = PaddingValues(0.dp)) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1500),
        label = "splash_animation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // Delay for 2 seconds
        navController.navigate(Destinations.GUIDE) {
            popUpTo(Destinations.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(insets),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.TwoTone.AutoAwesome,
            contentDescription = "Splash Screen Icon",
            modifier = Modifier.scale(scale.value)
        )
    }
}