package com.example.engine

import com.example.data.db.GoalProgressEntity
import com.example.data.db.ScenarioTurnEntity

object AntiRepetitionGuard {

    /**
     * Checks if the proposed coach response repeats a question or phrase from recent turns.
     * Returns true if too similar.
     */
    fun isTooSimilar(proposedText: String, recentTurns: List<ScenarioTurnEntity>): Boolean {
        if (proposedText.isBlank() || recentTurns.isEmpty()) return false

        val proposedLower = proposedText.lowercase().trim()
        val coachTurns = recentTurns.filter { it.speaker == "COACH" }.takeLast(8)

        for (turn in coachTurns) {
            val previousLower = turn.text.lowercase().trim()
            if (proposedLower == previousLower) return true

            // Calculate Jaccard similarity of words
            val similarity = calculateJaccardSimilarity(proposedLower, previousLower)
            if (similarity > 0.75) {
                return true
            }
        }
        return false
    }

    /**
     * Ensures that completed goals are not asked again in the session prompt instructions.
     */
    fun buildCompletedGoalsConstraint(completedGoals: List<GoalProgressEntity>): String {
        if (completedGoals.isEmpty()) return ""

        val completedTextList = completedGoals.filter { it.isCompleted }.map { "- Goal ID '${it.goalId}': ${it.goalText}" }
        if (completedTextList.isEmpty()) return ""

        return """
            CRITICAL ANTI-REPETITION CONSTRAINT:
            The user has ALREADY COMPLETED the following sub-goals in this session:
            ${completedTextList.joinToString("\n")}
            
            DO NOT ask the user for this information again. DO NOT repeat these questions.
            Move the conversation forward to payment, next step, new complication, or session wrap-up!
        """.trimIndent()
    }

    private fun calculateJaccardSimilarity(text1: String, text2: String): Double {
        val words1 = text1.split("\\s+".toRegex()).map { it.replace("[^a-zA-Z0-9]".toRegex(), "") }.filter { it.length > 2 }.toSet()
        val words2 = text2.split("\\s+".toRegex()).map { it.replace("[^a-zA-Z0-9]".toRegex(), "") }.filter { it.length > 2 }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return intersection.toDouble() / union.toDouble()
    }
}
