package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val nativeLang: String,
    val englishLevel: String,
    val purpose: String,
    val dailyGoalMins: Int,
    val interestsCsv: String,
    val correctionMode: String,
    val selectedCoach: String,
    val preferredViewMode: String,
    val preferredSpeechRate: String
)

@Entity(tableName = "scenario_sessions")
data class ScenarioSessionEntity(
    @PrimaryKey val sessionId: String,
    val scenarioId: String,
    val scenarioTitle: String,
    val coachName: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val isCompleted: Boolean = false,
    val turnsCount: Int = 0,
    val userSpokenSeconds: Int = 0,
    val primaryGoalMet: Boolean = false,
    val summaryFeedback: String = ""
)

@Entity(tableName = "scenario_turns")
data class ScenarioTurnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val turnNumber: Int,
    val speaker: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goal_progress")
data class GoalProgressEntity(
    @PrimaryKey val id: String, // "sessionId_goalId"
    val sessionId: String,
    val goalId: String,
    val goalText: String,
    val isCompleted: Boolean,
    val completedAt: Long = 0L
)

@Entity(tableName = "corrections")
data class CorrectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val userSentence: String,
    val correctedSentence: String,
    val trExplanation: String,
    val naturalAlternative: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vocabulary_items")
data class VocabularyItemEntity(
    @PrimaryKey val word: String,
    val phonetic: String,
    val definition: String,
    val partOfSpeech: String, // Verb, Noun, Adjective...
    val exampleSentence: String,
    val synonymsCsv: String,
    val status: String, // "NEW", "REVIEW", "MASTERED"
    val addedAt: Long = System.currentTimeMillis(),
    val nextReviewTime: Long = System.currentTimeMillis(),
    val reviewIntervalDays: Int = 1,
    val reviewCount: Int = 0
)

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val dateIso: String, // e.g. "2026-08-08"
    val minutesSpoken: Int,
    val goalsMet: Int,
    val sessionsCount: Int,
    val wordsLearned: Int
)

@Entity(tableName = "user_facts")
data class UserFactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val factKey: String,
    val factValue: String,
    val learnedAt: Long = System.currentTimeMillis()
)
