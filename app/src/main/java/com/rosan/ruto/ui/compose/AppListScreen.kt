package com.rosan.ruto.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.ruto.ui.Destinations
import com.rosan.ruto.ui.model.AppItem
import com.rosan.ruto.ui.viewmodel.AppListUiState
import com.rosan.ruto.ui.viewmodel.AppListViewModel
import com.rosan.ruto.ui.viewmodel.SortBy
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(navController: NavController, insets: WindowInsets) {
    val viewModel: AppListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apps") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Sort options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text("Sort by Name") }, onClick = {
                            viewModel.sortApps(SortBy.NAME)
                            showMenu = false
                        })
                        DropdownMenuItem(text = { Text("Sort by Package Name") }, onClick = {
                            viewModel.sortApps(SortBy.PACKAGE_NAME)
                            showMenu = false
                        })
                    }
                }
            )
        },
        contentWindowInsets = insets
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AppListUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is AppListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.apps, key = { it.appInfo.packageName }) { item ->
                            ListItem(
                                headlineContent = { Text(item.label) },
                                supportingContent = { Text(item.appInfo.packageName) },
                                leadingContent = {
                                    Image(
                                        bitmap = item.icon.asImageBitmap(),
                                        contentDescription = "${item.label} icon",
                                        modifier = Modifier.size(40.dp)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    selectedApp = item
                                    showDialog = true
                                }
                            )
                        }
                    }
                }

                is AppListUiState.Error -> {
                    Text(text = state.message)
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            appItem = selectedApp,
            onDismiss = { showDialog = false },
            onConfirm = { apiKey, hostUrl, modelId, task ->
                showDialog = false
                val encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8.toString())
                val encodedHostUrl = URLEncoder.encode(hostUrl, StandardCharsets.UTF_8.toString())
                val encodedModelId = URLEncoder.encode(modelId, StandardCharsets.UTF_8.toString())
                val encodedTask = URLEncoder.encode(task, StandardCharsets.UTF_8.toString())
                navController.navigate(
                    "${Destinations.TASK_EXECUTION}/${selectedApp?.appInfo?.packageName}/$encodedApiKey/$encodedHostUrl/$encodedModelId/$encodedTask"
                )
            }
        )
    }
}

@Composable
private fun TaskDialog(
    appItem: AppItem?,
    onDismiss: () -> Unit,
    onConfirm: (apiKey: String, hostUrl: String, modelId: String, task: String) -> Unit
) {
    if (appItem == null) return

    var hostUrl by remember { mutableStateOf("https://open.bigmodel.cn/api/paas/v4/") }
    var modelId by remember { mutableStateOf("autoglm-phone") }
    var apiKey by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("打开${appItem.label}，然后任务完成！") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Task for ${appItem.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = hostUrl, onValueChange = { hostUrl = it }, label = { Text("Host URL") })
                OutlinedTextField(value = modelId, onValueChange = { modelId = it }, label = { Text("Model ID") })
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") })
                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text("Task Requirement") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(apiKey, hostUrl, modelId, task)
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