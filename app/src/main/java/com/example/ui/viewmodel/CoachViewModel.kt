package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognizerManager
import com.example.audio.TextToSpeechManager
import com.example.audio.TtsEngineType
import com.example.data.db.AppDatabase
import com.example.data.db.GoalProgressEntity
import com.example.data.db.ScenarioSessionEntity
import com.example.data.db.ScenarioTurnEntity
import com.example.data.model.CoachTutor
import com.example.data.model.ConversationViewMode
import com.example.data.model.Scenario
import com.example.data.model.ScenarioCatalogRepository
import com.example.data.model.StructuredTurnResult
import com.example.data.model.UserProfile
import com.example.data.model.VoiceState
import com.example.data.repository.CoachRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoachViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CoachRepository(db)

    val speechRecognizer = SpeechRecognizerManager(application)
    val textToSpeech = TextToSpeechManager(application)

    private val _activeSession = MutableStateFlow<ScenarioSessionEntity?>(null)
    val activeSession: StateFlow<ScenarioSessionEntity?> = _activeSession.asStateFlow()

    private val _activeScenario = MutableStateFlow<Scenario?>(null)
    val activeScenario: StateFlow<Scenario?> = _activeScenario.asStateFlow()

    private val _turns = MutableStateFlow<List<ScenarioTurnEntity>>(emptyList())
    val turns: StateFlow<List<ScenarioTurnEntity>> = _turns.asStateFlow()

    private val _goals = MutableStateFlow<List<GoalProgressEntity>>(emptyList())
    val goals: StateFlow<List<GoalProgressEntity>> = _goals.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _currentViewMode = MutableStateFlow(ConversationViewMode.HYBRID)
    val currentViewMode: StateFlow<ConversationViewMode> = _currentViewMode.asStateFlow()

    private val _selectedCoach = MutableStateFlow(CoachTutor.MAYA)
    val selectedCoach: StateFlow<CoachTutor> = _selectedCoach.asStateFlow()

    private val _latestFeedback = MutableStateFlow<StructuredTurnResult?>(null)
    val latestFeedback: StateFlow<StructuredTurnResult?> = _latestFeedback.asStateFlow()

    private val _subtitlesEnabled = MutableStateFlow(true)
    val subtitlesEnabled: StateFlow<Boolean> = _subtitlesEnabled.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(true)
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    private var turnsCollectorJob: Job? = null
    private var goalsCollectorJob: Job? = null

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            _userProfile.value = profile
            _currentViewMode.value = profile.preferredViewMode
            _selectedCoach.value = profile.selectedCoach

            // Listen to speech recognizer final text
            launch {
                speechRecognizer.finalText.collect { text ->
                    if (text.isNotBlank() && _voiceState.value == VoiceState.TRANSCRIBING) {
                        processUserSpeechInput(text)
                    }
                }
            }

            // Listen to TTS speaking state
            launch {
                textToSpeech.isSpeaking.collect { isSpeaking ->
                    if (!isSpeaking && _voiceState.value == VoiceState.SPEAKING) {
                        _voiceState.value = VoiceState.IDLE
                    }
                }
            }
        }
    }

    fun startScenarioSession(scenarioId: String) {
        viewModelScope.launch {
            val scenario = ScenarioCatalogRepository.getById(scenarioId)
            _activeScenario.value = scenario

            val session = repository.startOrResumeSession(scenarioId)
            _activeSession.value = session

            // Observe turns for this session
            turnsCollectorJob?.cancel()
            turnsCollectorJob = launch {
                repository.getTurnsForSession(session.sessionId).collectLatest { turnList ->
                    _turns.value = turnList

                    // Speak last turn if it's from coach and autoSpeak is on
                    val lastTurn = turnList.lastOrNull()
                    if (lastTurn != null && lastTurn.speaker == "COACH" && _autoSpeakEnabled.value && _voiceState.value == VoiceState.IDLE) {
                        speakCoachResponse(lastTurn.text)
                    }
                }
            }

            // Observe goal progress
            goalsCollectorJob?.cancel()
            goalsCollectorJob = launch {
                repository.getGoalsForSession(session.sessionId).collectLatest { goalList ->
                    _goals.value = goalList
                }
            }
        }
    }

    fun startListening() {
        textToSpeech.stop() // Auto-cancel coach TTS playback when user taps mic
        _voiceState.value = VoiceState.LISTENING
        speechRecognizer.startListening()
    }

    fun stopListeningAndSubmit() {
        speechRecognizer.stopListening()
        val currentText = speechRecognizer.partialText.value
        _voiceState.value = VoiceState.TRANSCRIBING

        viewModelScope.launch {
            delay(300) // Brief pause for final text resolution
            val finalRecognized = speechRecognizer.finalText.value.ifBlank { currentText }
            if (finalRecognized.isNotBlank()) {
                processUserSpeechInput(finalRecognized)
            } else {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    fun processUserTextInput(text: String) {
        if (text.isBlank()) return
        textToSpeech.stop()
        viewModelScope.launch {
            processUserSpeechInput(text)
        }
    }

    private suspend fun processUserSpeechInput(text: String) {
        val session = _activeSession.value ?: return
        _voiceState.value = VoiceState.THINKING
        speechRecognizer.clear()

        val result = repository.processUserTurn(session.sessionId, text)
        _latestFeedback.value = result

        _voiceState.value = VoiceState.SYNTHESIZING
        speakCoachResponse(result.coachResponseText)
    }

    private fun speakCoachResponse(text: String) {
        _voiceState.value = VoiceState.SPEAKING
        textToSpeech.speak(
            text = text,
            coach = _selectedCoach.value,
            speechRate = if (_userProfile.value.preferredSpeechRate == "SLOW") 0.85f else 1.0f
        )
    }

    fun setViewMode(mode: ConversationViewMode) {
        _currentViewMode.value = mode
        viewModelScope.launch {
            repository.saveUserProfile(_userProfile.value.copy(preferredViewMode = mode))
        }
    }

    fun toggleCoachTutor() {
        val nextCoach = if (_selectedCoach.value == CoachTutor.MAYA) CoachTutor.LEO else CoachTutor.MAYA
        _selectedCoach.value = nextCoach
        viewModelScope.launch {
            val updated = _userProfile.value.copy(selectedCoach = nextCoach)
            _userProfile.value = updated
            repository.saveUserProfile(updated)
        }
    }

    fun setTtsEngine(type: TtsEngineType, userConsent: Boolean = false) {
        textToSpeech.setEngineType(type, userConsent)
    }

    fun toggleSubtitles() {
        _subtitlesEnabled.value = !_subtitlesEnabled.value
    }

    fun endSessionAndNavigateSummary(onNavigate: (String) -> Unit) {
        val session = _activeSession.value ?: return
        textToSpeech.stop()
        speechRecognizer.stopListening()
        viewModelScope.launch {
            repository.finishSessionManually(session.sessionId)
            onNavigate(session.sessionId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech.release()
        speechRecognizer.clear()
    }
}
