package com.example.engine

import com.example.data.model.GrammarCorrection
import com.example.data.model.StructuredTurnResult
import org.json.JSONArray
import org.json.JSONObject

object CoachResponseParser {

    fun parse(rawOutput: String): StructuredTurnResult {
        val cleanText = rawOutput.trim()

        // Extract JSON if wrapped in ```json ... ``` code blocks
        val jsonString = if (cleanText.contains("```json")) {
            cleanText.substringAfter("```json").substringBefore("```").trim()
        } else if (cleanText.contains("```")) {
            cleanText.substringAfter("```").substringBefore("```").trim()
        } else if (cleanText.startsWith("{") && cleanText.endsWith("}")) {
            cleanText
        } else {
            val startIdx = cleanText.indexOf('{')
            val endIdx = cleanText.lastIndexOf('}')
            if (startIdx != -1 && endIdx > startIdx) {
                cleanText.substring(startIdx, endIdx + 1)
            } else {
                null
            }
        }

        if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)
                val responseText = json.optString("coachResponseText", "").ifBlank {
                    json.optString("response", "")
                }

                val completedGoalIds = mutableListOf<String>()
                val jsonGoals = json.optJSONArray("completedGoalIds")
                if (jsonGoals != null) {
                    for (i in 0 until jsonGoals.length()) {
                        completedGoalIds.add(jsonGoals.getString(i))
                    }
                }

                val newComplication = json.optString("newComplication", "").ifBlank { null }
                val endDecision = json.optBoolean("endSessionDecision", false)
                val nextPrompt = json.optString("nextSuggestedPrompt", "").ifBlank { null }

                var feedbackCorrection: GrammarCorrection? = null
                val corrObj = json.optJSONObject("feedbackCorrection")
                if (corrObj != null && corrObj.has("userSentence") && corrObj.has("correctedSentence")) {
                    val uSent = corrObj.optString("userSentence", "")
                    val cSent = corrObj.optString("correctedSentence", "")
                    if (uSent.isNotBlank() && cSent.isNotBlank() && uSent != cSent) {
                        feedbackCorrection = GrammarCorrection(
                            userSentence = uSent,
                            correctedSentence = cSent,
                            trExplanation = corrObj.optString("trExplanation", "Daha doğal bir ifade."),
                            naturalAlternative = corrObj.optString("naturalAlternative", cSent),
                            category = corrObj.optString("category", "Grammar")
                        )
                    }
                }

                val targetWords = mutableListOf<String>()
                val jsonWords = json.optJSONArray("targetWordsIdentified")
                if (jsonWords != null) {
                    for (i in 0 until jsonWords.length()) {
                        targetWords.add(jsonWords.getString(i))
                    }
                }

                if (responseText.isNotBlank()) {
                    return StructuredTurnResult(
                        coachResponseText = responseText,
                        completedGoalIds = completedGoalIds,
                        newComplication = newComplication,
                        feedbackCorrection = feedbackCorrection,
                        targetWordsIdentified = targetWords,
                        endSessionDecision = endDecision,
                        nextSuggestedPrompt = nextPrompt
                    )
                }
            } catch (e: Exception) {
                // Fallback to text parsing
            }
        }

        // Fallback for non-JSON or malformed outputs
        return StructuredTurnResult(
            coachResponseText = cleanText,
            completedGoalIds = emptyList(),
            newComplication = null,
            feedbackCorrection = null,
            targetWordsIdentified = emptyList(),
            endSessionDecision = false
        )
    }
}
