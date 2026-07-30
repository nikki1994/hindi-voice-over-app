package com.example.audio

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

sealed class TtsEngineStatus {
    object Initializing : TtsEngineStatus()
    data class Ready(val voicesCount: Int, val currentVoiceName: String?) : TtsEngineStatus()
    data class HindiNotSupported(val message: String) : TtsEngineStatus()
    data class Error(val message: String) : TtsEngineStatus()
}

enum class VoicePreset(
    val titleHindi: String,
    val titleEnglish: String,
    val pitch: Float,
    val rate: Float,
    val description: String
) {
    NATURAL("प्राकृतिक", "Natural Standard", 1.0f, 1.0f, "Standard balanced Hindi pronunciation"),
    WARM_CALM("शांत व गम्भीर", "Warm & Calm", 0.85f, 0.85f, "Soothing, relaxed tone for stories and podcasts"),
    NEWS_ANCHOR("समाचार वाचक", "News Anchor", 1.05f, 1.15f, "Clear, fast, energetic news reader style"),
    STORYTELLER("कहानीकार", "Storyteller", 0.95f, 0.90f, "Expressive voice with natural pauses"),
    FAST_READER("द्रुत वाचक", "Fast Reader", 1.0f, 1.35f, "Quick playback for long articles and study notes")
}

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _status = MutableStateFlow<TtsEngineStatus>(TtsEngineStatus.Initializing)
    val status: StateFlow<TtsEngineStatus> = _status.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    private val _synthesisProgress = MutableStateFlow(0f)
    val synthesisProgress: StateFlow<Float> = _synthesisProgress.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setupHindiLanguage()
        } else {
            _status.value = TtsEngineStatus.Error("Failed to initialize Android Text-To-Speech engine.")
        }
    }

    private fun setupHindiLanguage() {
        val ttsEngine = tts ?: return
        val hindiLocale = Locale("hi", "IN")
        val result = ttsEngine.setLanguage(hindiLocale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Try fallback Locale("hi")
            val fallbackResult = ttsEngine.setLanguage(Locale("hi"))
            if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                _status.value = TtsEngineStatus.HindiNotSupported(
                    "Hindi voice pack is not installed on this device. Please install Hindi in Android Speech Settings."
                )
                return
            }
        }

        // Setup progress listener
        ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId?.startsWith("synth_") == true) {
                        _isSynthesizing.value = true
                        _synthesisProgress.value = 0.2f
                    } else {
                        _isPlaying.value = true
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId?.startsWith("synth_") == true) {
                        _isSynthesizing.value = false
                        _synthesisProgress.value = 1.0f
                    } else {
                        _isPlaying.value = false
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    _isPlaying.value = false
                    _isSynthesizing.value = false
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    _isPlaying.value = false
                    _isSynthesizing.value = false
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                // Can track spoken word range
            }
        })

        // Filter available voices for Hindi
        val hindiVoices = try {
            ttsEngine.voices?.filter { voice ->
                voice.locale.language == "hi" || voice.locale.country == "IN"
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val currentVoiceName = ttsEngine.voice?.name
        _status.value = TtsEngineStatus.Ready(hindiVoices.size, currentVoiceName)
    }

    fun speak(text: String, pitch: Float = 1.0f, rate: Float = 1.0f) {
        if (text.isBlank()) return
        val ttsEngine = tts ?: return
        
        ttsEngine.stop()
        ttsEngine.setPitch(pitch)
        ttsEngine.setSpeechRate(rate)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "preview_${System.currentTimeMillis()}")
        }
        ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "preview_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _isSynthesizing.value = false
    }

    fun synthesizeToFile(
        text: String,
        pitch: Float,
        rate: Float,
        targetFile: File,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val ttsEngine = tts
        if (ttsEngine == null) {
            onComplete(false, "TTS engine not initialized")
            return
        }

        ttsEngine.stop()
        ttsEngine.setPitch(pitch)
        ttsEngine.setSpeechRate(rate)

        _isSynthesizing.value = true
        _synthesisProgress.value = 0.1f

        val utteranceId = "synth_${System.currentTimeMillis()}"

        // Set custom temporary listener for file synthesis
        val originalListener = ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                mainHandler.post {
                    _synthesisProgress.value = 0.5f
                }
            }

            override fun onDone(id: String?) {
                mainHandler.post {
                    _isSynthesizing.value = false
                    _synthesisProgress.value = 1.0f
                    if (id == utteranceId && targetFile.exists() && targetFile.length() > 0) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, "Audio file generated was empty.")
                    }
                    // Restore main listener
                    setupHindiLanguage()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                mainHandler.post {
                    _isSynthesizing.value = false
                    onComplete(false, "Error synthesizing audio file.")
                    setupHindiLanguage()
                }
            }

            override fun onError(id: String?, errorCode: Int) {
                mainHandler.post {
                    _isSynthesizing.value = false
                    onComplete(false, "Error synthesizing audio (Code $errorCode)")
                    setupHindiLanguage()
                }
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = ttsEngine.synthesizeToFile(text, params, targetFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            _isSynthesizing.value = false
            onComplete(false, "Failed to launch TTS file synthesis.")
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
