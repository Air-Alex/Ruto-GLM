package com.rosan.ruto.ui.compose

import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.ui.Destinations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeBakScreen(navController: NavController, insets: PaddingValues) {
    val device: DeviceRepo = koinInject()
    val scope = rememberCoroutineScope()

    // State for display and UI
    var displayId by remember { mutableStateOf<Int?>(null) }
    var surface by remember { mutableStateOf<Surface?>(null) }
    var packageName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AndroidView to host the SurfaceView
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { SurfaceView(it) },
            update = { view ->
                surface = view.holder.surface
            }
        )

        // Input field for package name
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = packageName,
            onValueChange = { packageName = it },
            label = { Text("Package Name") },
            placeholder = { Text("e.g., com.android.settings") },
            enabled = displayId != null
        )

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Button to create a display and bind it to the surface
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val currentSurface = surface ?: return@launch
                        val newId = device.displayManager.createDisplay2(currentSurface)
                        if (newId != -1) {
                            displayId = newId
                        }
                        Log.e("HomeScreen", "newId:$newId displayId: $displayId")
                    }
                },
                enabled = displayId == null && surface != null
            ) {
                Text("Create & Bind")
            }

            // Button to release the created display
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val id = displayId ?: return@launch
                        device.displayManager.release(id)
                        displayId = null
                    }
                },
                enabled = displayId != null
            ) {
                Text("Release")
            }

            // Button to launch an app on the created display
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val id = displayId ?: return@launch
                        if (packageName.isNotBlank()) {
                            Log.e("HomeScreen", "packageName: $packageName, newId: $id")
                            device.activityManager.startApp(packageName, id)
                        }
                    }
                },
                enabled = displayId != null && packageName.isNotBlank()
            ) {
                Text("Launch App")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Destinations.APP_LIST) },
                enabled = true
            ) {
                Text("Show All Apps")
            }
        }
    }
}