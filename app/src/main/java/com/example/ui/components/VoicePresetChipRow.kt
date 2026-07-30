package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.VoicePreset

@Composable
fun VoicePresetChipRow(
    selectedPreset: VoicePreset,
    onPresetSelected: (VoicePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "आवाज़ का स्वरूप (Voice Profile):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VoicePreset.entries.forEach { preset ->
                val isSelected = preset == selectedPreset
                FilterChip(
                    selected = isSelected,
                    onClick = { onPresetSelected(preset) },
                    label = {
                        Text(
                            text = "${preset.titleHindi} (${preset.titleEnglish})",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("preset_chip_${preset.name.lowercase()}")
                )
            }
        }
    }
}
