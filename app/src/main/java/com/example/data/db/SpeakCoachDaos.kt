package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface ScenarioSessionDao {
    @Query("SELECT * FROM scenario_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ScenarioSessionEntity>>

    @Query("SELECT * FROM scenario_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<ScenarioSessionEntity?>

    @Query("SELECT * FROM scenario_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): ScenarioSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScenarioSessionEntity)

    @Update
    suspend fun updateSession(session: ScenarioSessionEntity)

    // Turns
    @Query("SELECT * FROM scenario_turns WHERE sessionId = :sessionId ORDER BY turnNumber ASC")
    fun getTurnsForSession(sessionId: String): Flow<List<ScenarioTurnEntity>>

    @Query("SELECT * FROM scenario_turns WHERE sessionId = :sessionId ORDER BY turnNumber ASC")
    suspend fun getTurnsForSessionOnce(sessionId: String): List<ScenarioTurnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: ScenarioTurnEntity)

    // Goal Progress
    @Query("SELECT * FROM goal_progress WHERE sessionId = :sessionId")
    fun getGoalsForSession(sessionId: String): Flow<List<GoalProgressEntity>>

    @Query("SELECT * FROM goal_progress WHERE sessionId = :sessionId")
    suspend fun getGoalsForSessionOnce(sessionId: String): List<GoalProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalProgress(goalProgress: GoalProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoalProgress(goals: List<GoalProgressEntity>)
}

@Dao
interface CorrectionDao {
    @Query("SELECT * FROM corrections ORDER BY timestamp DESC")
    fun getAllCorrections(): Flow<List<CorrectionEntity>>

    @Query("SELECT * FROM corrections WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getCorrectionsForSession(sessionId: String): List<CorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: CorrectionEntity)
}

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary_items ORDER BY addedAt DESC")
    fun getAllVocabulary(): Flow<List<VocabularyItemEntity>>

    @Query("SELECT * FROM vocabulary_items WHERE status = 'REVIEW' OR nextReviewTime <= :currentTime ORDER BY nextReviewTime ASC")
    fun getReviewQueue(currentTime: Long): Flow<List<VocabularyItemEntity>>

    @Query("SELECT * FROM vocabulary_items WHERE word = :word")
    suspend fun getWord(word: String): VocabularyItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(item: VocabularyItemEntity)

    @Update
    suspend fun updateVocabulary(item: VocabularyItemEntity)

    @Query("SELECT COUNT(*) FROM vocabulary_items")
    fun getVocabularyCount(): Flow<Int>
}

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity ORDER BY dateIso DESC")
    fun getAllDailyActivities(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE dateIso = :dateIso")
    suspend fun getActivityForDate(dateIso: String): DailyActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: DailyActivityEntity)

    @Query("SELECT SUM(minutesSpoken) FROM daily_activity")
    fun getTotalMinutesSpoken(): Flow<Int?>
}

@Dao
interface UserFactDao {
    @Query("SELECT * FROM user_facts ORDER BY learnedAt DESC")
    fun getAllUserFacts(): Flow<List<UserFactEntity>>

    @Query("SELECT * FROM user_facts ORDER BY learnedAt DESC")
    suspend fun getAllUserFactsOnce(): List<UserFactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: UserFactEntity)
}
