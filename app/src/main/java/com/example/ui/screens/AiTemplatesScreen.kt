package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.GeminiScriptHelper
import com.example.ai.ScriptMode
import com.example.ui.VoiceoverViewModel

data class ScriptTemplateCategory(
    val titleHindi: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val scripts: List<String>
)

val TEMPLATE_CATEGORIES = listOf(
    ScriptTemplateCategory(
        titleHindi = "समाचार व बुलेटिन",
        titleEnglish = "News & Announcements",
        icon = Icons.Default.Newspaper,
        scripts = listOf(
            "ताज़ा खबर: देश भर में मौसम विभाग ने आज हल्की बारिश की संभावना जताई है। नागरिकों से सतर्क रहने की अपील की गई है।",
            "खेल समाचार: भारतीय टीम ने आज एक कड़े मुकाबले में ऐतिहासिक जीत हासिल कर श्रृंखला अपने नाम कर ली है।"
        )
    ),
    ScriptTemplateCategory(
        titleHindi = "कहानी व पॉडकास्ट",
        titleEnglish = "Story & Podcasts",
        icon = Icons.Default.MenuBook,
        scripts = listOf(
            "हिमालय की वादियों में स्थित उस छोटे से गाँव में एक पुरानी कहावत प्रसिद्ध थी। जो भी उस नदी को पार करता, उसे अपने प्रश्नों के उत्तर मिल जाते।",
            "सुनिए आज की प्रेरणादायक कहानी। जब परिस्थितियाँ प्रतिकूल हों, तब धैर्य ही मनुष्य का सबसे बड़ा मित्र होता है।"
        )
    ),
    ScriptTemplateCategory(
        titleHindi = "विज्ञापन व प्रचार",
        titleEnglish = "Ads & Commercials",
        icon = Icons.Default.Campaign,
        scripts = listOf(
            "क्या आप भी अपनी दिनचर्या में नई ताज़गी चाहते हैं? आज ही आज़माएं हमारा नया और प्राकृतिक उत्पाद!",
            "महा छूट! इस सप्ताहांत पाएं अपने पसंदीदा सामान पर 50% तक की भारी छूट। ऑफर सीमित समय तक उपलब्ध है।"
        )
    ),
    ScriptTemplateCategory(
        titleHindi = "शिक्षा व ट्यूटोरियल",
        titleEnglish = "Education & Tutorials",
        icon = Icons.Default.School,
        scripts = listOf(
            "नमस्ते विद्यार्थियों! आज के इस पाठ में हम सौर मंडल और ग्रहों की गतियों के बारे में विस्तार से समझेंगे।",
            "सफलता पाने के लिए तीन नियम याद रखें: निरंतर अभ्यास, एकाग्रता, और समय का सही प्रबंधन।"
        )
    )
)

@Composable
fun AiTemplatesScreen(
    viewModel: VoiceoverViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var englishInput by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Quick Translator Box
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "अंग्रेज़ी से हिन्दी AI अनुवादक",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = englishInput,
                    onValueChange = { englishInput = it },
                    placeholder = { Text("Type English text to translate to Hindi voiceover script...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (englishInput.isBlank()) return@Button
                        isTranslating = true
                        viewModel.updateText("अनुवाद किया जा रहा है...")
                        viewModel.enhanceWithAi(ScriptMode.TRANSLATE_ENGLISH_TO_HINDI)
                        isTranslating = false
                        Toast.makeText(context, "हिन्दी में अनुवाद हो गया!", Toast.LENGTH_SHORT).show()
                        onNavigateToStudio()
                    },
                    enabled = englishInput.isNotBlank() && !isTranslating,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translate & Send to Studio", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Text(
            text = "तैयार हिन्दी स्क्रिप्ट्स (Ready Scripts):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(TEMPLATE_CATEGORIES) { category ->
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${category.titleHindi} (${category.titleEnglish})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        category.scripts.forEachIndexed { idx, scriptText ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = scriptText,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.updateText(scriptText)
                                            Toast.makeText(context, "स्क्रिप्ट स्टूडियो में जोड़ दी गई", Toast.LENGTH_SHORT).show()
                                            onNavigateToStudio()
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Use Script")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
