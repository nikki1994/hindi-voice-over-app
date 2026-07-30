package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SampleHindiText(
    val title: String,
    val content: String
)

val DEFAULT_SAMPLES = listOf(
    SampleHindiText(
        "सुप्रभात (Greeting)",
        "सुप्रभात! आपका आज का दिन ढेर सारी सफलता, शांति और प्रसन्नता से भरा हो।"
    ),
    SampleHindiText(
        "समाचार (News)",
        "ताज़ा खबर: आज भारत में तकनीकी विकास के क्षेत्र में एक नया इतिहास रचा गया है।"
    ),
    SampleHindiText(
        "कहानी (Story)",
        "एक समय की बात है, एक सुंदर और शांत गाँव में एक दयालु किसान रहता था। उसके विचार सदैव सकारात्मक रहते थे।"
    ),
    SampleHindiText(
        "सुविचार (Quote)",
        "विश्वास वह शक्ति है जिससे उजड़ी हुई दुनिया में भी प्रकाश लाया जा सकता है। मेहनत ही सफलता की कुंजी है।"
    ),
    SampleHindiText(
        "ऑडियो गाइड (Guide)",
        "नमस्ते! हमारी ऑडियो गाइड सेवा में आपका स्वागत है। चलिए आज की यात्रा शुरू करते हैं।"
    )
)

@Composable
fun SampleTextChips(
    onSampleSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "नमूना हिन्दी पाठ (Sample Texts):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DEFAULT_SAMPLES.forEachIndexed { index, sample ->
                AssistChip(
                    onClick = { onSampleSelect(sample.content) },
                    label = { Text(text = sample.title) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("sample_chip_$index")
                )
            }
        }
    }
}
