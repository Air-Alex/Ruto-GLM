package com.rosan.ruto.ui.compose.screen_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rosan.ruto.ui.viewmodel.DisplayItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenListItem(
    display: DisplayItem,
    isSelected: Boolean,
    onDelete: (Int) -> Unit,
    onPreview: (Int) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = if (isSelected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                Text("#${display.displayId} ${display.name}")
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Text(
                    """
                                        UniqueId: ${display.uniqueId}
                                        Resolution: ${display.logicalWidth}x${display.logicalHeight}
                                        DPI: ${display.logicalDensityDpi}
                                        """.trimIndent()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.End
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = display.isMyDisplay) {
                    IconButton(
                        onClick = { onDelete(display.displayId) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }

                IconButton(
                    onClick = { onPreview(display.displayId) }
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "预览")
                }
            }
        }
    }
}