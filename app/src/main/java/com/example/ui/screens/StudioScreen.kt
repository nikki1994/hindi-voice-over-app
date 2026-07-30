package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ScriptMode
import com.example.audio.TtsEngineStatus
import com.example.ui.VoiceoverViewModel
import com.example.ui.components.ExportResultCard
import com.example.ui.components.SampleTextChips
import com.example.ui.components.VoicePresetChipRow
import com.example.ui.components.WaveformVisualizer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: VoiceoverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hindiText by viewModel.hindiTextInput.collectAsState()
    val customTitle by viewModel.customTitle.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val pitch by viewModel.speechPitch.collectAsState()
    val rate by viewModel.speechRate.collectAsState()

    val ttsStatus by viewModel.ttsStatus.collectAsState()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsState()
    val isSynthesizing by viewModel.isSynthesizing.collectAsState()
    val synthesisProgress by viewModel.synthesisProgress.collectAsState()

    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val exportStatusMessage by viewModel.exportStatusMessage.collectAsState()
    val lastExportedItem by viewModel.lastExportedItem.collectAsState()
    val currentlyPlayingSavedId by viewModel.currentlyPlayingSavedId.collectAsState()

    var showControlsDrawer by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning banner if Hindi TTS is missing or needs setup
        if (ttsStatus is TtsEngineStatus.HindiNotSupported) {
            val missingMsg = (ttsStatus as TtsEngineStatus.HindiNotSupported).message
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "हिन्दी वॉइस पैक इंस्टॉल नहीं है",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = missingMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    TextButton(onClick = { viewModel.openTtsSettings(context) }) {
                        Text("सेटिंग्स (Settings)")
                    }
                }
            }
        }

        // Hero Header Banner
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "हिन्दी वॉइस स्टूडियो (Hindi Studio)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "पाठ को प्राकृतिक वॉइसओवर और MP3 में बदलें",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Waveform Audio Visualizer Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    WaveformVisualizer(isPlaying = isPlayingPreview)
                }
            }
        }

        // Title Input (Optional)
        OutlinedTextField(
            value = customTitle,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("शीर्षक (Audio Title - Optional)") },
            placeholder = { Text("उदा. मेरा पहला वॉइसओवर") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_title_input"),
            shape = RoundedCornerShape(16.dp)
        )

        // Main Hindi Text Input Area
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "हिन्दी इनपुट टेक्स्ट (Hindi Input Text):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        // Paste button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                    viewModel.updateText(pasted)
                                    Toast.makeText(context, "पेस्ट किया गया", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste Text",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Copy button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Hindi Text", hindiText))
                                Toast.makeText(context, "कॉपी हो गया", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Clear button
                        IconButton(
                            onClick = { viewModel.updateText("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hindiText,
                    onValueChange = { viewModel.updateText(it) },
                    placeholder = { Text("यहाँ हिन्दी में टेक्स्ट टाइप या पेस्ट करें...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hindi_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val wordCount = hindiText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                    Text(
                        text = "अक्षर: ${hindiText.length} | शब्द: $wordCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val estSec = (hindiText.length / (12.0 * rate)).toInt()
                    Text(
                        text = "अनुमानित अवधि: ~${estSec}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Assist Action Chips
                Text(
                    text = "AI टेक्स्ट सहायक (AI Text Assistant):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.enhanceWithAi(ScriptMode.ENHANCE_FOR_TTS) },
                        enabled = !isAiLoading && hindiText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_enhance_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("विराम चिह्न", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.enhanceWithAi(ScriptMode.TRANSLATE_ENGLISH_TO_HINDI) },
                        enabled = !isAiLoading && hindiText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_translate_button")
                    ) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("हिन्दी अनुवाद", fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Sample Chips
        SampleTextChips(
            onSampleSelect = { sample ->
                viewModel.updateText(sample)
            }
        )

        // Voice Controls Section (Preset profiles + Pitch & Rate Sliders)
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                VoicePresetChipRow(
                    selectedPreset = selectedPreset,
                    onPresetSelected = { preset -> viewModel.selectPreset(preset) }
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "बारीक स्वर नियंत्रण (Detailed Tuning):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(onClick = { showControlsDrawer = !showControlsDrawer }) {
                        Text(if (showControlsDrawer) "छिपाएं" else "दिखाएं")
                    }
                }

                AnimatedVisibility(visible = showControlsDrawer) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Pitch Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "पिच (Pitch / स्वर ऊँचाई): ${String.format(Locale.getDefault(), "%.2fx", pitch)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = if (pitch < 1f) "गंभीर (Deep)" else if (pitch > 1f) "तीक्ष्ण (High)" else "सामान्य", style = MaterialTheme.typography.labelSmall)
                        }
                        Slider(
                            value = pitch,
                            onValueChange = { viewModel.updatePitch(it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("pitch_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Speech Rate / Speed Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "गति (Speech Speed / गति): ${String.format(Locale.getDefault(), "%.2fx", rate)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = if (rate < 1f) "धीमी (Slow)" else if (rate > 1f) "तेज (Fast)" else "सामान्य", style = MaterialTheme.typography.labelSmall)
                        }
                        Slider(
                            value = rate,
                            onValueChange = { viewModel.updateRate(it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("speed_slider")
                        )
                    }
                }
            }
        }

        // Primary Synthesis Actions
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (isSynthesizing) {
                    Text(
                        text = "MP3 फाइल बनाई जा रही है... कृपया प्रतीक्षा करें",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { synthesisProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Preview Audio button
                    OutlinedButton(
                        onClick = {
                            if (isPlayingPreview) {
                                viewModel.stopPlayback()
                            } else {
                                viewModel.previewSpeech()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("preview_speech_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isPlayingPreview) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPlayingPreview) "रुकें" else "सुनें (Preview)")
                    }

                    // Export MP3 button
                    Button(
                        onClick = { viewModel.exportVoiceoverToMp3(context) },
                        enabled = !isSynthesizing && hindiText.isNotBlank(),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1.3f)
                            .testTag("export_mp3_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MP3 बनाएं (Export)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Newly Exported MP3 Result Card Banner
        lastExportedItem?.let { exportedItem ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExportResultCard(
                    item = exportedItem,
                    isPlaying = currentlyPlayingSavedId == exportedItem.id,
                    onPlayToggle = { viewModel.playSavedVoiceover(context, exportedItem) },
                    onShare = { viewModel.shareVoiceover(context, exportedItem) },
                    onDownloadToPhone = { viewModel.saveToPublicDownloads(context, exportedItem) },
                    onDismiss = { viewModel.clearStatusMessage() }
                )
            }
        }
    }
}
