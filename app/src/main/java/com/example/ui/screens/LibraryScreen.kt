package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.AudioExportHelper
import com.example.data.VoiceoverEntity
import com.example.ui.VoiceoverViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: VoiceoverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceovers by viewModel.savedVoiceovers.collectAsState()
    val currentlyPlayingId by viewModel.currentlyPlayingSavedId.collectAsState()

    var filterFavoritesOnly by remember { mutableStateOf(false) }

    val filteredList = if (filterFavoritesOnly) {
        voiceovers.filter { it.isFavorite }
    } else {
        voiceovers
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ऑडियो लाइब्रेरी (MP3 Library)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "कुल ${voiceovers.size} सेव की गई MP3 फाइलें",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterFavoritesOnly,
                    onClick = { filterFavoritesOnly = false },
                    label = { Text("सभी (${voiceovers.size})") },
                    modifier = Modifier.testTag("filter_all_chip")
                )
                FilterChip(
                    selected = filterFavoritesOnly,
                    onClick = { filterFavoritesOnly = true },
                    label = { Text("पसंदीदा (${voiceovers.count { it.isFavorite }})") },
                    modifier = Modifier.testTag("filter_fav_chip")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filterFavoritesOnly) "कोई पसंदीदा वॉइसओवर नहीं है" else "अभी तक कोई MP3 नहीं बनाया गया है",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "स्टूडियो में हिन्दी टेक्स्ट से नई MP3 फाइलें बनाएं",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("voiceover_list")
            ) {
                items(filteredList, key = { it.id }) { item ->
                    VoiceoverCardItem(
                        item = item,
                        isPlaying = currentlyPlayingId == item.id,
                        onPlayClick = { viewModel.playSavedVoiceover(context, item) },
                        onShareClick = { viewModel.shareVoiceover(context, item) },
                        onDownloadClick = { viewModel.saveToPublicDownloads(context, item) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item) },
                        onDeleteClick = { viewModel.deleteVoiceover(item) },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Hindi Voiceover Text", item.hindiText))
                            Toast.makeText(context, "हिन्दी टेक्स्ट कॉपी हो गया", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceoverCardItem(
    item: VoiceoverEntity,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onShareClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyText: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateString = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "समय: ${AudioExportHelper.formatDuration(item.durationMs)} • $dateString • ${item.fileSizeFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorite Heart Button
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hindi Text Snippet Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.hindiText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Copy Text button
                    IconButton(onClick = onCopyText, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Text", modifier = Modifier.size(18.dp))
                    }

                    // Share MP3
                    IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share MP3", modifier = Modifier.size(18.dp))
                    }

                    // Save to Downloads
                    IconButton(onClick = onDownloadClick, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download MP3", modifier = Modifier.size(18.dp))
                    }
                }

                // Delete Button
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
