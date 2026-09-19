package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.WearDialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil3.compose.AsyncImage
import com.yorkyang2333.claudwecho.ui.components.WearListHeader
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import com.yorkyang2333.claudwecho.ui.components.rotaryContentPadding

@Composable
fun PlaylistDetailInfoDialog(
    showDialog: Boolean,
    detail: ResourceDetailInfo?,
    onDismissRequest: () -> Unit
) {
    WearDialog(
        visible = showDialog,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (detail != null) {
                val headerTitle = when (detail.type) {
                    "album" -> "专辑详情"
                    "djradio" -> "播客详情"
                    else -> "歌单详情"
                }

                RotaryScalingLazyColumn(
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = rotaryContentPadding(bottomItemHeight = 44.dp)
                ) {
                    item {
                        WearListHeader(title = headerTitle)
                    }

                    if (!detail.coverUrl.isNullOrBlank()) {
                        item {
                            AsyncImage(
                                model = detail.coverUrl,
                                contentDescription = detail.title,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            text = detail.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!detail.subtitle.isNullOrBlank()) {
                        item {
                            InfoField(
                                label = when (detail.type) {
                                    "album" -> "歌手"
                                    "djradio" -> "主播"
                                    else -> "创建者"
                                },
                                value = detail.subtitle.removePrefix("创建者: ").removePrefix("歌手: ").removePrefix("主播: ")
                            )
                        }
                    }

                    item {
                        InfoField(
                            label = "曲目数",
                            value = "${detail.trackCount} 首"
                        )
                    }

                    if (!detail.publishDate.isNullOrBlank()) {
                        item {
                            InfoField(
                                label = if (detail.type == "album") "发行日期" else "创建时间",
                                value = detail.publishDate
                            )
                        }
                    }

                    if (detail.tags.isNotEmpty()) {
                        item {
                            InfoField(
                                label = "标签",
                                value = detail.tags.joinToString(" / ")
                            )
                        }
                    }

                    if (!detail.description.isNullOrBlank()) {
                        item {
                            InfoField(
                                label = "简介",
                                value = detail.description.trim()
                            )
                        }
                    }

                    item {
                        InfoField(
                            label = "ID",
                            value = detail.id.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis
        )
    }
}

