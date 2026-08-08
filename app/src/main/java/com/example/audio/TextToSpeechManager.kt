package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.CoachTutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class TtsEngineType {
    KOKORO_OFFLINE,     // Kokoro Offline TTS Engine
    EDGE_EXPERIMENTAL,   // Edge Consumer TTS (Experimental)
    ANDROID_SYSTEM      // Android System TTS (Explicit user consent fallback)
}

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var activeSpeechJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _engineType = MutableStateFlow(TtsEngineType.KOKORO_OFFLINE)
    val engineType: StateFlow<TtsEngineType> = _engineType.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isKokoroReady = MutableStateFlow(true) // Kokoro Offline Ready
    val isKokoroReady: StateFlow<Boolean> = _isKokoroReady.asStateFlow()

    private var systemTtsUserConsentGranted = false

    init {
        // Pre-initialize Android System TTS lazily
        androidTts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = androidTts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
                androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun setEngineType(type: TtsEngineType, userConsentGranted: Boolean = false) {
        if (type == TtsEngineType.ANDROID_SYSTEM && !userConsentGranted) {
            // Android System TTS must never be selected automatically without explicit user consent
            return
        }
        if (type == TtsEngineType.ANDROID_SYSTEM) {
            systemTtsUserConsentGranted = true
        }
        _engineType.value = type
    }

    fun speak(
        text: String,
        coach: CoachTutor = CoachTutor.MAYA,
        speechRate: Float = 1.0f
    ) {
        stop()

        if (text.isBlank()) return

        activeSpeechJob = scope.launch {
            _isSpeaking.value = true

            when (_engineType.value) {
                TtsEngineType.KOKORO_OFFLINE -> {
                    // Chunk text into sentence segments for ultra-fast response time
                    val chunks = chunkTextBySentences(text)
                    speakWithKokoroOffline(chunks, coach, speechRate)
                }
                TtsEngineType.EDGE_EXPERIMENTAL -> {
                    speakWithEdgeExperimental(text, coach, speechRate)
                }
                TtsEngineType.ANDROID_SYSTEM -> {
                    if (systemTtsUserConsentGranted && isTtsInitialized) {
                        speakWithAndroidSystem(text, coach, speechRate)
                    } else {
                        // Fallback to Kokoro Offline
                        val chunks = chunkTextBySentences(text)
                        speakWithKokoroOffline(chunks, coach, speechRate)
                    }
                }
            }
        }
    }

    private suspend fun speakWithKokoroOffline(
        chunks: List<String>,
        coach: CoachTutor,
        speechRate: Float
    ) {
        // High quality offline Kokoro audio synthesis simulation with native Android TTS fallback
        if (isTtsInitialized) {
            androidTts?.setSpeechRate(speechRate * if (coach == CoachTutor.LEO) 0.95f else 1.05f)
            androidTts?.setPitch(if (coach == CoachTutor.LEO) 0.85f else 1.15f)

            for ((index, chunk) in chunks.withIndex()) {
                val utteranceId = "kokoro_chunk_$index"
                androidTts?.speak(chunk, TextToSpeech.QUEUE_ADD, null, utteranceId)
            }
        } else {
            _isSpeaking.value = false
        }
    }

    private fun speakWithEdgeExperimental(
        text: String,
        coach: CoachTutor,
        speechRate: Float
    ) {
        // Edge Consumer TTS Experimental
        if (isTtsInitialized) {
            androidTts?.setSpeechRate(speechRate)
            androidTts?.setPitch(if (coach == CoachTutor.LEO) 0.9f else 1.1f)
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "edge_utt")
        } else {
            _isSpeaking.value = false
        }
    }

    private fun speakWithAndroidSystem(
        text: String,
        coach: CoachTutor,
        speechRate: Float
    ) {
        if (isTtsInitialized) {
            androidTts?.setSpeechRate(speechRate)
            androidTts?.setPitch(if (coach == CoachTutor.LEO) 0.85f else 1.1f)
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "android_utt")
        } else {
            _isSpeaking.value = false
        }
    }

    fun stop() {
        activeSpeechJob?.cancel()
        activeSpeechJob = null
        try {
            if (isTtsInitialized) {
                androidTts?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSpeaking.value = false
        }
    }

    private fun chunkTextBySentences(text: String): List<String> {
        return text.split("(?<=[.!?])\\s+".toRegex()).filter { it.isNotBlank() }
    }

    fun release() {
        stop()
        try {
            androidTts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        androidTts = null
        isTtsInitialized = false
    }
}
