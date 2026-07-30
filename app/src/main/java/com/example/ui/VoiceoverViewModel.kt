package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiScriptHelper
import com.example.ai.ScriptMode
import com.example.audio.AudioExportHelper
import com.example.audio.TtsEngineStatus
import com.example.audio.TtsManager
import com.example.audio.VoicePreset
import com.example.data.VoiceoverDatabase
import com.example.data.VoiceoverEntity
import com.example.data.VoiceoverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class VoiceoverViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoiceoverRepository
    val ttsManager: TtsManager = TtsManager(application)

    val ttsStatus: StateFlow<TtsEngineStatus> = ttsManager.status
    val isPlayingPreview: StateFlow<Boolean> = ttsManager.isPlaying
    val isSynthesizing: StateFlow<Boolean> = ttsManager.isSynthesizing
    val synthesisProgress: StateFlow<Float> = ttsManager.synthesisProgress

    val savedVoiceovers: StateFlow<List<VoiceoverEntity>>

    private val _hindiTextInput = MutableStateFlow(
        "नमस्ते! हिन्दी वॉइसओवर स्टूडियो में आपका स्वागत है। यहाँ आप किसी भी पाठ को प्राकृतिक हिन्दी आवाज़ में बदल सकते हैं और MP3 के रूप में एक्सपोर्ट कर सकते हैं।"
    )
    val hindiTextInput: StateFlow<String> = _hindiTextInput.asStateFlow()

    private val _selectedPreset = MutableStateFlow(VoicePreset.NATURAL)
    val selectedPreset: StateFlow<VoicePreset> = _selectedPreset.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _customTitle = MutableStateFlow("")
    val customTitle: StateFlow<String> = _customTitle.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    val exportStatusMessage: StateFlow<String?> = _exportStatusMessage.asStateFlow()

    private val _lastExportedItem = MutableStateFlow<VoiceoverEntity?>(null)
    val lastExportedItem: StateFlow<VoiceoverEntity?> = _lastExportedItem.asStateFlow()

    private val _currentlyPlayingSavedId = MutableStateFlow<Long?>(null)
    val currentlyPlayingSavedId: StateFlow<Long?> = _currentlyPlayingSavedId.asStateFlow()

    init {
        val dao = VoiceoverDatabase.getDatabase(application).voiceoverDao()
        repository = VoiceoverRepository(dao)

        savedVoiceovers = repository.allVoiceovers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateText(text: String) {
        _hindiTextInput.value = text
    }

    fun updateTitle(title: String) {
        _customTitle.value = title
    }

    fun selectPreset(preset: VoicePreset) {
        _selectedPreset.value = preset
        _speechPitch.value = preset.pitch
        _speechRate.value = preset.rate
    }

    fun updatePitch(pitch: Float) {
        _speechPitch.value = pitch
    }

    fun updateRate(rate: Float) {
        _speechRate.value = rate
    }

    fun previewSpeech() {
        val text = _hindiTextInput.value
        if (text.isBlank()) return
        ttsManager.speak(text, _speechPitch.value, _speechRate.value)
    }

    fun stopPlayback() {
        ttsManager.stop()
        AudioExportHelper.stopAudioPlayback()
        _currentlyPlayingSavedId.value = null
    }

    fun enhanceWithAi(mode: ScriptMode) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = GeminiScriptHelper.translateOrEnhanceToHindi(_hindiTextInput.value, mode)
            result.onSuccess { generatedText ->
                _hindiTextInput.value = generatedText
            }
            _isAiLoading.value = false
        }
    }

    fun exportVoiceoverToMp3(context: Context) {
        val text = _hindiTextInput.value.trim()
        if (text.isBlank()) {
            Toast.makeText(context, "कृपया पहले हिन्दी पाठ दर्ज करें", Toast.LENGTH_SHORT).show()
            return
        }

        stopPlayback()
        _exportStatusMessage.value = "MP3 फाइल बनाई जा रही है..."

        val targetFile = AudioExportHelper.createTempAudioFile(context)
        
        ttsManager.synthesizeToFile(
            text = text,
            pitch = _speechPitch.value,
            rate = _speechRate.value,
            targetFile = targetFile,
            onComplete = { success, errorMsg ->
                if (success && targetFile.exists()) {
                    viewModelScope.launch {
                        val derivedTitle = _customTitle.value.ifBlank {
                            if (text.length > 25) text.take(25) + "..." else text
                        }
                        val formattedSize = AudioExportHelper.formatFileSize(targetFile.length())
                        val durationMs = AudioExportHelper.estimateDurationMs(text.length, _speechRate.value)

                        val entity = VoiceoverEntity(
                            title = derivedTitle,
                            hindiText = text,
                            filePath = targetFile.absolutePath,
                            fileName = targetFile.name,
                            fileSizeFormatted = formattedSize,
                            durationMs = durationMs,
                            speechRate = _speechRate.value,
                            speechPitch = _speechPitch.value
                        )

                        val id = repository.insert(entity)
                        val savedEntity = entity.copy(id = id)
                        _lastExportedItem.value = savedEntity
                        _exportStatusMessage.value = "MP3 सफलतापूर्वक बनाई गई! (${formattedSize})"
                    }
                } else {
                    _exportStatusMessage.value = "त्रुटि: ${errorMsg ?: "MP3 सेव नहीं हो सका"}"
                }
            }
        )
    }

    fun shareVoiceover(context: Context, entity: VoiceoverEntity) {
        val file = File(entity.filePath)
        if (file.exists()) {
            AudioExportHelper.shareAudioFile(context, file, entity.title)
        } else {
            Toast.makeText(context, "ऑडियो फाइल नहीं मिली", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveToPublicDownloads(context: Context, entity: VoiceoverEntity) {
        val file = File(entity.filePath)
        if (file.exists()) {
            val uri = AudioExportHelper.exportToPublicDownloads(context, file, entity.title)
            if (uri != null) {
                Toast.makeText(context, "Downloads/HindiVoiceStudio में MP3 सेव हो गया!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "MP3 डाउनलोड में सेव नहीं हो सका", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "फाइल उपलब्ध नहीं है", Toast.LENGTH_SHORT).show()
        }
    }

    fun playSavedVoiceover(context: Context, entity: VoiceoverEntity) {
        if (_currentlyPlayingSavedId.value == entity.id) {
            AudioExportHelper.stopAudioPlayback()
            _currentlyPlayingSavedId.value = null
            return
        }

        stopPlayback()
        val file = File(entity.filePath)
        if (file.exists()) {
            _currentlyPlayingSavedId.value = entity.id
            AudioExportHelper.playAudioFile(context, file) {
                _currentlyPlayingSavedId.value = null
            }
        } else {
            Toast.makeText(context, "ऑडियो फाइल नहीं मिली", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(entity: VoiceoverEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(entity)
        }
    }

    fun deleteVoiceover(entity: VoiceoverEntity) {
        viewModelScope.launch {
            if (_currentlyPlayingSavedId.value == entity.id) {
                AudioExportHelper.stopAudioPlayback()
                _currentlyPlayingSavedId.value = null
            }
            val file = File(entity.filePath)
            if (file.exists()) {
                file.delete()
            }
            repository.delete(entity)
        }
    }

    fun clearStatusMessage() {
        _exportStatusMessage.value = null
    }

    fun openTtsSettings(context: Context) {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "TTS सेटिंग्स नहीं खोली जा सकीं", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
        AudioExportHelper.stopAudioPlayback()
    }
}
