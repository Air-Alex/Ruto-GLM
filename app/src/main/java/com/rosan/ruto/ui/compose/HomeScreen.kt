package com.rosan.ruto.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.ruto.ui.Destinations
import com.rosan.ruto.ui.viewmodel.HomeViewModel
import com.rosan.ruto.util.SettingsManager
import org.koin.androidx.compose.koinViewModel

const val NEW_DISPLAY_ID = -1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, insets: WindowInsets) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showConfigDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruto") },
                actions = {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        contentWindowInsets = insets
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Shizuku") },
                    supportingContent = { Text(uiState.shizukuVersion) },
                    leadingContent = {
                        Icon(
                            imageVector = if (uiState.isShizukuReady) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Shizuku Status"
                        )
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Keep Alive Service") },
                    supportingContent = { Text(if (uiState.isKeepAliveServiceRunning) "Running" else "Not Running") },
                    leadingContent = {
                        Icon(
                            imageVector = if (uiState.isKeepAliveServiceRunning) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Service Status"
                        )
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showConfigDialog = true }
            ) {
                ListItem(
                    headlineContent = { Text("Model Configuration") },
                    supportingContent = { Text("Click to edit configuration") },
                    leadingContent = {
                        Icon(Icons.Default.Settings, contentDescription = "Model Configuration")
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showTaskDialog = true }
            ) {
                ListItem(
                    headlineContent = { Text("Execute Task") },
                    supportingContent = { Text("Click to enter a task to execute") },
                    leadingContent = {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Execute Task")
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { navController.navigate(Destinations.TASK_LIST) }
            ) {
                ListItem(
                    headlineContent = { Text("Running Tasks") },
                    supportingContent = { Text("Click to view running tasks") },
                    leadingContent = {
                        Icon(Icons.Default.List, contentDescription = "Running Tasks")
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { navController.navigate(Destinations.SCREEN_LIST) }
            ) {
                ListItem(
                    headlineContent = { Text("Screen List") },
                    supportingContent = { Text("Click to view screen list") },
                    leadingContent = {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = "Screen List")
                    }
                )
            }
        }
    }

    if (showConfigDialog) {
        ModelConfigDialog(
            onDismiss = { showConfigDialog = false },
            onConfirm = { showConfigDialog = false }
        )
    }

    if (showTaskDialog) {
        val context = LocalContext.current
        TaskInputDialog(
            physicalDisplayIds = uiState.physicalDisplayIds,
            onDismiss = { showTaskDialog = false },
            onConfirm = { task, displayId ->
                showTaskDialog = false

                val hostUrl = SettingsManager.getHostUrl(context)
                val apiKey = SettingsManager.getApiKey(context)
                val modelId = SettingsManager.getModelId(context)

                viewModel.executeTask(
                    packageName = "",
                    apiKey = apiKey,
                    hostUrl = hostUrl,
                    modelId = modelId,
                    task = task,
                    displayId = displayId
                )
            }
        )
    }
}

@Composable
private fun ModelConfigDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current
    var hostUrl by remember { mutableStateOf(SettingsManager.getHostUrl(context)) }
    var apiKey by remember { mutableStateOf(SettingsManager.getApiKey(context)) }
    var modelId by remember { mutableStateOf(SettingsManager.getModelId(context)) }

    var isHostUrlError by remember { mutableStateOf(false) }
    var isApiKeyError by remember { mutableStateOf(false) }
    var isModelIdError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Model Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hostUrl,
                    onValueChange = { hostUrl = it; isHostUrlError = false },
                    label = { Text("Host URL") },
                    isError = isHostUrlError
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; isApiKeyError = false },
                    label = { Text("API Key") },
                    isError = isApiKeyError
                )
                OutlinedTextField(
                    value = modelId,
                    onValueChange = { modelId = it; isModelIdError = false },
                    label = { Text("Model ID") },
                    isError = isModelIdError
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                isHostUrlError = hostUrl.isBlank()
                isApiKeyError = apiKey.isBlank()
                isModelIdError = modelId.isBlank()
                if (!isHostUrlError && !isApiKeyError && !isModelIdError) {
                    SettingsManager.saveSettings(context, hostUrl, apiKey, modelId)
                    onConfirm()
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskInputDialog(
    physicalDisplayIds: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (task: String, displayId: Int) -> Unit
) {
    var task by remember { mutableStateOf("打开抖音，不停的往下翻") }
    var isTaskError by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val options = listOf("New Screen") + physicalDisplayIds.map { "Screen $it" }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it; isTaskError = false },
                    label = { Text("Task Requirement") },
                    isError = isTaskError
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedOptionText,
                        onValueChange = { },
                        label = { Text("Display") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedOptionText = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                isTaskError = task.isBlank()
                if (!isTaskError) {
                    val displayId = if (selectedOptionText == "New Screen") {
                        NEW_DISPLAY_ID
                    } else {
                        selectedOptionText.removePrefix("Screen ").toInt()
                    }
                    onConfirm(task, displayId)
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
