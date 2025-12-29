package com.rosan.ruto.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.ruto.service.TaskStatus
import com.rosan.ruto.ui.Destinations
import com.rosan.ruto.ui.viewmodel.TaskListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
private fun ExpandableText(text: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = text,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .animateContentSize()
            .clickable { expanded = !expanded }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, insets: WindowInsets) {
    val viewModel: TaskListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Running Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = insets
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.tasks.toList()) { (key, task) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Task ID: $key")
                        ExpandableText(text = "Task: ${task.task}")
                        Text(text = "Display ID: ${task.displayId}")
                        Text(text = "Status: ${task.status}")
                        task.statusMessage?.let {
                            ExpandableText(text = "Message: $it")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (task.status == TaskStatus.RUNNING || task.status == TaskStatus.THINKING) {
                                Button(onClick = { viewModel.stopTask(key) }) {
                                    Text("Stop")
                                }
                                Button(onClick = { navController.navigate("${Destinations.TASK_PREVIEW}/$key") }) {
                                    Text("Preview")
                                }
                            } else {
                                Button(onClick = { viewModel.stopTask(key) }) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}