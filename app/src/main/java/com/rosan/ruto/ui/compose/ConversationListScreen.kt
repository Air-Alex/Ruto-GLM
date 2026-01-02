package com.rosan.ruto.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.twotone.Forum
import androidx.compose.material.icons.twotone.HourglassTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.ruto.data.model.AiType
import com.rosan.ruto.data.model.ConversationModel
import com.rosan.ruto.ui.Destinations
import com.rosan.ruto.viewmodel.ConversationListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    navController: NavController,
    insets: WindowInsets,
    viewModel: ConversationListViewModel = koinViewModel()
) {
    val conversations by viewModel.conversations.collectAsState(initial = emptyList())
    val isLoading by remember { mutableStateOf(false) }

    var selectedIds by remember { mutableStateOf(emptyList<Long>()) }
    val isInSelectionMode = selectedIds.isNotEmpty()

    var showDialog by remember { mutableStateOf(false) }

    fun toggleSelection(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isInSelectionMode) "${selectedIds.size} selected" else "Conversations")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isInSelectionMode) selectedIds =
                            emptyList() else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isInSelectionMode) {
                        IconButton(onClick = { selectedIds = conversations.map { it.id } }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = isInSelectionMode) {
                    FloatingActionButton(
                        onClick = {
                            if (selectedIds.isEmpty()) return@FloatingActionButton
                            viewModel.remove(selectedIds)
                            selectedIds = emptyList() // 删除后清空选择
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }
            }
        },
        contentWindowInsets = insets
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (conversations.isEmpty()) {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    EmptyConversation()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        // 增加 animateItem 使得增删列表时有位移动画
                        Box(modifier = Modifier.animateItem()) {
                            ConversationListItem(
                                conversation = conversation,
                                isSelected = conversation.id in selectedIds,
                                onClick = {
                                    if (isInSelectionMode) toggleSelection(conversation.id)
                                    else navController.navigate("${Destinations.CONVERSATION}/${conversation.id}")
                                },
                                onLongClick = { toggleSelection(conversation.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateConversationDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, modelId, hostUrl, apiKey, aiType ->
                viewModel.add(
                    ConversationModel(
                        aiType = aiType,
                        hostUrl = hostUrl,
                        modelId = modelId,
                        apiKey = apiKey,
                        name = name
                    )
                )
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateConversationDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, modelId: String, hostUrl: String, apiKey: String, aiType: AiType) -> Unit
) {
    var name by remember { mutableStateOf("New Conversation") }
    var modelId by remember { mutableStateOf("gpt-3.5-turbo") }
    var hostUrl by remember { mutableStateOf("https://api.openai.com/v1/") }
    var apiKey by remember { mutableStateOf("") }

    // 控制密码可见性的状态
    var apiKeyVisible by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var selectedAiType by remember { mutableStateOf(AiType.OpenAI) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Conversation") },
        text = {
            // 使用 VerticalScroll 避免在小屏手机上内容溢出
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedAiType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("AI Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AiType.entries.forEach { aiType -> // 建议使用 entries 代替 values()
                            DropdownMenuItem(
                                text = { Text(aiType.name) },
                                onClick = {
                                    selectedAiType = aiType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hostUrl,
                    onValueChange = { hostUrl = it },
                    label = { Text("Host URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = modelId,
                    onValueChange = { modelId = it },
                    label = { Text("Model ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                // API Key 密码输入框
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    // 核心逻辑 1：视觉变换
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    // 核心逻辑 2：键盘类型设定
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // 核心逻辑 3：添加切换按钮
                    trailingIcon = {
                        val image = if (apiKeyVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = "Toggle API Key Visibility"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name, modelId, hostUrl, apiKey, selectedAiType)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationListItem(
    conversation: ConversationModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "borderColor"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "borderWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = animatedBorderWidth,
                color = animatedBorderColor,
                shape = MaterialTheme.shapes.medium
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp // 选中时稍微悬浮
        )
    ) {
        ListItem(
            headlineContent = {
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    label = "contentColor"
                )
                Text(
                    conversation.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = animatedColor
                )
            },
            supportingContent = {
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    label = "contentColor"
                )
                Text(
                    "Model: ${conversation.modelId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedColor
                )
            },
            leadingContent = {
                // 图标切换使用淡入淡出动画
                Crossfade(targetState = isSelected, label = "iconFade") { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Default.SelectAll else Icons.TwoTone.HourglassTop,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        )
    }
}

@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "scale"
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            Icons.TwoTone.HourglassTop,
            contentDescription = null,
            modifier = Modifier.scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyConversation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.TwoTone.Forum,
                contentDescription = "Empty conversation",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Text(
                text = "No conversations yet. Start a new one!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}