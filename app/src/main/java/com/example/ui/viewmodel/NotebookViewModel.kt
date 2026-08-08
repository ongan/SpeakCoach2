package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CorrectionEntity
import com.example.data.db.VocabularyItemEntity
import com.example.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotebookViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = VocabularyRepository(db)

    val vocabularyList: StateFlow<List<VocabularyItemEntity>> = repository.allVocabulary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val correctionsList: StateFlow<List<CorrectionEntity>> = repository.allCorrections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _reviewQueue = MutableStateFlow<List<VocabularyItemEntity>>(emptyList())
    val reviewQueue: StateFlow<List<VocabularyItemEntity>> = _reviewQueue.asStateFlow()

    private val _currentReviewIndex = MutableStateFlow(0)
    val currentReviewIndex: StateFlow<Int> = _currentReviewIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialVocabularyIfEmpty()
            repository.allVocabulary.collect { list ->
                _reviewQueue.value = list
            }
        }
    }

    fun submitReviewResult(remembered: Boolean) {
        val currentItem = _reviewQueue.value.getOrNull(_currentReviewIndex.value) ?: return
        viewModelScope.launch {
            repository.updateReviewResult(currentItem.word, remembered)
            _isCardFlipped.value = false
            if (_currentReviewIndex.value < _reviewQueue.value.size - 1) {
                _currentReviewIndex.value += 1
            } else {
                _currentReviewIndex.value = 0
            }
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun addCustomWord(word: String, definition: String, example: String) {
        if (word.isBlank()) return
        viewModelScope.launch {
            repository.saveVocabulary(
                VocabularyItemEntity(
                    word = word.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    phonetic = "/${word.lowercase()}/",
                    definition = definition.ifBlank { "Custom saved word from practice." },
                    partOfSpeech = "Expression",
                    exampleSentence = example.ifBlank { "I practiced using '$word' in conversation." },
                    synonymsCsv = "practice",
                    status = "NEW"
                )
            )
        }
    }
}
