package com.example.engine

import com.example.data.db.GoalProgressEntity
import com.example.data.db.ScenarioTurnEntity
import com.example.data.db.UserFactEntity
import com.example.data.model.CoachTutor
import com.example.data.model.CorrectionMode
import com.example.data.model.Scenario
import com.example.data.model.UserProfile

object TurnPlanner {

    fun buildSystemInstruction(
        scenario: Scenario,
        profile: UserProfile,
        coach: CoachTutor,
        completedGoals: List<GoalProgressEntity>,
        currentTurnNumber: Int,
        userFacts: List<UserFactEntity>
    ): String {
        val completedIds = completedGoals.filter { it.isCompleted }.map { it.goalId }
        val remainingGoals = scenario.subGoals.filter { it.goalId !in completedIds }

        val goalsStatusText = scenario.subGoals.joinToString("\n") { goal ->
            val isDone = goal.goalId in completedIds
            val status = if (isDone) "[COMPLETED]" else "[PENDING]"
            "- ID: '${goal.goalId}' | Text: '${goal.text}' | Status: $status"
        }

        val factsText = if (userFacts.isEmpty()) "None recorded yet." else userFacts.take(5).joinToString(", ") { "${it.factKey}: ${it.factValue}" }

        val antiRepetitionConstraint = AntiRepetitionGuard.buildCompletedGoalsConstraint(completedGoals)

        val correctionGuidance = when (profile.correctionMode) {
            CorrectionMode.FLOW -> "Do NOT interrupt the conversation flow with immediate corrections unless requested. Keep feedback strictly for the end of the session."
            CorrectionMode.BALANCED -> "If the user made a notable grammar or vocabulary slip in their last response, include a brief, supportive feedbackCorrection object in your JSON result without breaking character."
            CorrectionMode.COACH -> "Actively highlight significant grammar errors in feedbackCorrection and encourage the user to try the natural phrasing."
        }

        val speechRateGuidance = when (profile.preferredSpeechRate) {
            "SLOW" -> "Use clear, concise sentences with simple structures for easier comprehension."
            "FAST" -> "Use rich native idiomatic expressions and fluid conversational speed."
            else -> "Use natural, authentic conversational English appropriate for a ${scenario.cefr} level learner."
        }

        return """
            You are ${coach.displayName}, an expert AI English Speaking Coach (${coach.title}).
            Persona: ${coach.persona}
            
            SCENARIO CONTEXT:
            - Title: ${scenario.title}
            - Primary Goal: ${scenario.primaryGoal}
            - Your Role in Roleplay: ${scenario.aiRole}
            - User's Role in Roleplay: ${scenario.userRole}
            - CEFR Target Level: ${scenario.cefr}
            - Current Turn Number: $currentTurnNumber (Min: ${scenario.minTurns}, Max: ${scenario.maxTurns})
            
            SUB-GOALS STATUS:
            $goalsStatusText
            
            REMAINING UNMET GOALS:
            ${if (remainingGoals.isEmpty()) "ALL SUB-GOALS MET! Prepare to conclude scenario if appropriate." else remainingGoals.joinToString("\n") { "- '${it.goalId}': ${it.text}" }}
            
            USER PROFILE & KNOWN FACTS:
            - Learner Name: ${profile.name}
            - Target Level: ${profile.englishLevel}
            - Known Learner Facts: $factsText
            
            CORRECTION POLICY (${profile.correctionMode.name}):
            $correctionGuidance
            
            SPEECH STYLE GUIDANCE:
            $speechRateGuidance
            
            $antiRepetitionConstraint
            
            STRICT RULES FOR YOUR RESPONSE:
            1. Respond directly to the user's latest statement in your role (${scenario.aiRole}).
            2. Never ask a question about something the user ALREADY answered or a goal marked [COMPLETED].
            3. Ask AT MOST ONE question in your response (or zero questions if advancing or wrapping up).
            4. If all mandatory sub-goals are met or turn limit is reached, set "endSessionDecision": true and offer a warm closing line.
            5. ALWAYS output your response as a valid JSON object strictly matching this schema:
            
            {
              "coachResponseText": "Your natural in-character response to the user",
              "completedGoalIds": ["goal_id_if_user_just_fulfilled_one"],
              "newComplication": "optional unexpected situation or null",
              "feedbackCorrection": {
                "userSentence": "exact phrase user said with error",
                "correctedSentence": "natural corrected English phrase",
                "trExplanation": "short Turkish explanation of the mistake",
                "naturalAlternative": "even more native sounding option",
                "category": "Grammar or Vocabulary"
              },
              "targetWordsIdentified": ["vocabulary_word_used"],
              "endSessionDecision": false,
              "nextSuggestedPrompt": "Optional quick tap suggestion for user's next reply"
            }
            
            If no correction is needed, set "feedbackCorrection": null.
            Return ONLY the raw JSON object.
        """.trimIndent()
    }
}
