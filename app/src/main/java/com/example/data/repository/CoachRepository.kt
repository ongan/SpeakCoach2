package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.db.CorrectionEntity
import com.example.data.db.GoalProgressEntity
import com.example.data.db.ScenarioSessionEntity
import com.example.data.db.ScenarioTurnEntity
import com.example.data.db.UserFactEntity
import com.example.data.db.UserProfileEntity
import com.example.data.db.VocabularyItemEntity
import com.example.data.model.CoachTutor
import com.example.data.model.CorrectionMode
import com.example.data.model.GrammarCorrection
import com.example.data.model.Scenario
import com.example.data.model.ScenarioCatalogRepository
import com.example.data.model.StructuredTurnResult
import com.example.data.model.UserProfile
import com.example.engine.AntiRepetitionGuard
import com.example.engine.CoachResponseParser
import com.example.engine.TurnPlanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CoachRepository(private val db: AppDatabase) {

    private val sessionDao = db.scenarioSessionDao()
    private val profileDao = db.userProfileDao()
    private val correctionDao = db.correctionDao()
    private val vocabularyDao = db.vocabularyDao()
    private val factDao = db.userFactDao()

    val activeSessionFlow: Flow<ScenarioSessionEntity?> = sessionDao.getActiveSession()
    val allSessionsFlow: Flow<List<ScenarioSessionEntity>> = sessionDao.getAllSessions()

    fun getTurnsForSession(sessionId: String): Flow<List<ScenarioTurnEntity>> =
        sessionDao.getTurnsForSession(sessionId)

    fun getGoalsForSession(sessionId: String): Flow<List<GoalProgressEntity>> =
        sessionDao.getGoalsForSession(sessionId)

    suspend fun getUserProfile(): UserProfile = withContext(Dispatchers.IO) {
        val entity = profileDao.getUserProfileOnce()
        if (entity != null) {
            UserProfile(
                name = entity.name,
                nativeLang = entity.nativeLang,
                englishLevel = entity.englishLevel,
                purpose = entity.purpose,
                dailyGoalMins = entity.dailyGoalMins,
                interests = entity.interestsCsv.split(",").filter { it.isNotBlank() },
                correctionMode = try { CorrectionMode.valueOf(entity.correctionMode) } catch (e: Exception) { CorrectionMode.BALANCED },
                selectedCoach = try { CoachTutor.valueOf(entity.selectedCoach) } catch (e: Exception) { CoachTutor.MAYA },
                preferredSpeechRate = entity.preferredSpeechRate
            )
        } else {
            val defaultProfile = UserProfile()
            saveUserProfile(defaultProfile)
            defaultProfile
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                name = profile.name,
                nativeLang = profile.nativeLang,
                englishLevel = profile.englishLevel,
                purpose = profile.purpose,
                dailyGoalMins = profile.dailyGoalMins,
                interestsCsv = profile.interests.joinToString(","),
                correctionMode = profile.correctionMode.name,
                selectedCoach = profile.selectedCoach.name,
                preferredViewMode = profile.preferredViewMode.name,
                preferredSpeechRate = profile.preferredSpeechRate
            )
        )
    }

    suspend fun startOrResumeSession(scenarioId: String): ScenarioSessionEntity = withContext(Dispatchers.IO) {
        val existingActive = sessionDao.getActiveSession().firstOrNull()
        if (existingActive != null && existingActive.scenarioId == scenarioId) {
            return@withContext existingActive
        }

        // Close any existing active session
        if (existingActive != null) {
            sessionDao.updateSession(existingActive.copy(isCompleted = true, endTime = System.currentTimeMillis()))
        }

        val scenario = ScenarioCatalogRepository.getById(scenarioId)
        val profile = getUserProfile()
        val sessionId = UUID.randomUUID().toString()

        val newSession = ScenarioSessionEntity(
            sessionId = sessionId,
            scenarioId = scenario.id,
            scenarioTitle = scenario.title,
            coachName = profile.selectedCoach.displayName,
            startTime = System.currentTimeMillis()
        )

        sessionDao.insertSession(newSession)

        // Initialize goal progress entities
        val goalProgressList = scenario.subGoals.map { goal ->
            GoalProgressEntity(
                id = "${sessionId}_${goal.goalId}",
                sessionId = sessionId,
                goalId = goal.goalId,
                goalText = goal.text,
                isCompleted = false
            )
        }
        sessionDao.insertAllGoalProgress(goalProgressList)

        // Add initial Coach Opener
        val openerText = scenario.openers.shuffled().first()
        sessionDao.insertTurn(
            ScenarioTurnEntity(
                sessionId = sessionId,
                turnNumber = 1,
                speaker = "COACH",
                text = openerText
            )
        )

        newSession
    }

    suspend fun processUserTurn(
        sessionId: String,
        userText: String
    ): StructuredTurnResult = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext StructuredTurnResult("I missed that, could you repeat?")
        val scenario = ScenarioCatalogRepository.getById(session.scenarioId)
        val profile = getUserProfile()
        val coach = try { CoachTutor.valueOf(session.coachName.uppercase()) } catch (e: Exception) { CoachTutor.MAYA }

        val currentTurns = sessionDao.getTurnsForSessionOnce(sessionId)
        val currentTurnNumber = currentTurns.size + 1

        // Insert User Turn
        sessionDao.insertTurn(
            ScenarioTurnEntity(
                sessionId = sessionId,
                turnNumber = currentTurnNumber,
                speaker = "USER",
                text = userText
            )
        )

        val updatedTurns = sessionDao.getTurnsForSessionOnce(sessionId)
        val completedGoals = sessionDao.getGoalsForSessionOnce(sessionId).filter { it.isCompleted }
        val userFacts = factDao.getAllUserFactsOnce()

        // Build system instruction & conversation history
        val systemInstruction = TurnPlanner.buildSystemInstruction(
            scenario = scenario,
            profile = profile,
            coach = coach,
            completedGoals = completedGoals,
            currentTurnNumber = updatedTurns.size,
            userFacts = userFacts
        )

        val history = updatedTurns.takeLast(10).map { it.speaker to it.text }

        // Call Gemini LLM Engine
        var rawLlmOutput = GeminiClient.generateResponse(
            systemInstruction = systemInstruction,
            userPrompt = userText,
            conversationHistory = history
        )

        // Parse structured result
        var parsedResult = CoachResponseParser.parse(rawLlmOutput)

        // Local Smart Fallback Engine if API key is missing or offline
        if (parsedResult.coachResponseText.isBlank()) {
            parsedResult = generateLocalSmartFallback(scenario, userText, completedGoals, updatedTurns.size)
        }

        // Anti-repetition check
        if (AntiRepetitionGuard.isTooSimilar(parsedResult.coachResponseText, updatedTurns)) {
            // Re-generate or refine
            parsedResult = parsedResult.copy(
                coachResponseText = "${parsedResult.coachResponseText} Let's move on—${scenario.complications.shuffled().first()}"
            )
        }

        // Save new completed goals in Room
        for (goalId in parsedResult.completedGoalIds) {
            val goalKey = "${sessionId}_$goalId"
            val goalText = scenario.subGoals.find { it.goalId == goalId }?.text ?: "Completed Goal"
            sessionDao.insertGoalProgress(
                GoalProgressEntity(
                    id = goalKey,
                    sessionId = sessionId,
                    goalId = goalId,
                    goalText = goalText,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        // Save Grammar Correction in Room if present
        parsedResult.feedbackCorrection?.let { correction ->
            correctionDao.insertCorrection(
                CorrectionEntity(
                    sessionId = sessionId,
                    userSentence = correction.userSentence,
                    correctedSentence = correction.correctedSentence,
                    trExplanation = correction.trExplanation,
                    naturalAlternative = correction.naturalAlternative,
                    category = correction.category
                )
            )
        }

        // Save Identified Vocabulary
        for (word in parsedResult.targetWordsIdentified) {
            if (word.length > 2) {
                vocabularyDao.insertVocabulary(
                    VocabularyItemEntity(
                        word = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        phonetic = "/${word.lowercase()}/",
                        definition = "Key vocabulary used during scenario: ${scenario.title}",
                        partOfSpeech = "Expression",
                        exampleSentence = "'${parsedResult.coachResponseText}'",
                        synonymsCsv = "practice, fluency",
                        status = "NEW"
                    )
                )
            }
        }

        // Save AI Coach Turn
        val coachTurnNumber = updatedTurns.size + 1
        sessionDao.insertTurn(
            ScenarioTurnEntity(
                sessionId = sessionId,
                turnNumber = coachTurnNumber,
                speaker = "COACH",
                text = parsedResult.coachResponseText
            )
        )

        // Check if mandatory goals are satisfied to update session state
        val allSessionGoals = sessionDao.getGoalsForSessionOnce(sessionId)
        val mandatoryGoalsMet = scenario.subGoals.filter { it.isMandatory }.all { mGoal ->
            allSessionGoals.any { it.goalId == mGoal.goalId && it.isCompleted }
        }

        val estimatedSpokenSeconds = (updatedTurns.filter { it.speaker == "USER" }.sumOf { it.text.split(" ").size } * 0.8).toInt()

        if (parsedResult.endSessionDecision || coachTurnNumber >= scenario.maxTurns) {
            sessionDao.updateSession(
                session.copy(
                    isCompleted = mandatoryGoalsMet,
                    turnsCount = coachTurnNumber,
                    userSpokenSeconds = estimatedSpokenSeconds,
                    primaryGoalMet = mandatoryGoalsMet,
                    endTime = System.currentTimeMillis()
                )
            )
        } else {
            sessionDao.updateSession(
                session.copy(
                    turnsCount = coachTurnNumber,
                    userSpokenSeconds = estimatedSpokenSeconds,
                    primaryGoalMet = mandatoryGoalsMet
                )
            )
        }

        parsedResult
    }

    private fun generateLocalSmartFallback(
        scenario: Scenario,
        userText: String,
        completedGoals: List<GoalProgressEntity>,
        turnCount: Int
    ): StructuredTurnResult {
        val completedIds = completedGoals.map { it.goalId }
        val remainingMandatory = scenario.subGoals.filter { it.isMandatory && it.goalId !in completedIds }

        val newlyCompletedGoal = remainingMandatory.firstOrNull()?.goalId
        val completedList = if (newlyCompletedGoal != null) listOf(newlyCompletedGoal) else emptyList()

        val textLower = userText.lowercase()
        val coachText = when {
            turnCount <= 2 -> "That sounds great! I've noted that. ${remainingMandatory.getOrNull(0)?.text ?: "How else can I assist you?"}"
            turnCount <= 4 -> "Perfect, got it! Let's confirm: ${remainingMandatory.getOrNull(0)?.text ?: "Everything is looking good on my end."}"
            else -> "Excellent work! You've handled all the key parts of this conversation with confidence."
        }

        val endDecision = remainingMandatory.isEmpty() || turnCount >= scenario.maxTurns

        return StructuredTurnResult(
            coachResponseText = coachText,
            completedGoalIds = completedList,
            newComplication = if (turnCount == 3) scenario.complications.shuffled().firstOrNull() else null,
            feedbackCorrection = if (textLower.contains("i want")) GrammarCorrection(
                userSentence = "I want",
                correctedSentence = "I would like / I'd like",
                trExplanation = "'I would like' ifadesi restoran ve otellerde daha kibar ve doğaldır.",
                naturalAlternative = "I'd like to order..."
            ) else null,
            targetWordsIdentified = listOf("Confirmation", "Order"),
            endSessionDecision = endDecision,
            nextSuggestedPrompt = "Sounds good to me!"
        )
    }

    suspend fun finishSessionManually(sessionId: String) = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext
        val goals = sessionDao.getGoalsForSessionOnce(sessionId)
        val primaryMet = goals.any { it.isCompleted }
        sessionDao.updateSession(
            session.copy(
                isCompleted = true,
                endTime = System.currentTimeMillis(),
                primaryGoalMet = primaryMet
            )
        )
    }
}
