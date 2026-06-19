package com.example.claudwecho.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.claudwecho.data.api.Song

@Composable
fun SharedSongItem(
    song: Song,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(),
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (song.fee == 1) {
                    Text(
                        text = "VIP",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        },
        secondaryLabel = {
            Text(
                text = song.ar.firstOrNull()?.name ?: "Unknown Artist",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            AsyncImage(
                model = song.al?.picUrl ?: "",
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    )
}
