package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.repository.AnalyticsRepository
import com.example.data.repository.AnalyticsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AnalyticsRepository(db)

    private val _summary = MutableStateFlow(
        AnalyticsSummary(
            totalMinutesSpoken = 14,
            completedScenariosCount = 3,
            totalSessionsCount = 4,
            goalCompletionRatePercent = 85,
            totalCorrectionsLogged = 5,
            totalVocabularyCount = 8,
            currentStreakDays = 4,
            estimatedCefrLevel = "B1 (Estimated)"
        )
    )
    val summary: StateFlow<AnalyticsSummary> = _summary.asStateFlow()

    init {
        viewModelScope.launch {
            repository.analyticsSummaryFlow.collect { summary ->
                _summary.value = summary
            }
        }
    }
}
