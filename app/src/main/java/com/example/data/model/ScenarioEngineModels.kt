package com.example.data.model

data class Goal(
    val goalId: String,
    val text: String,
    val isMandatory: Boolean = true
)

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Daily Life", "Career", "Travel", "Debate", "Social"
    val cefr: String, // "A1", "A2", "B1", "B2", "C1"
    val durationMins: Int,
    val iconName: String,
    val accentHex: String,
    val primaryGoal: String,
    val subGoals: List<Goal>,
    val userRole: String,
    val aiRole: String,
    val aiPersonality: String,
    val openers: List<String>,
    val complications: List<String>,
    val minTurns: Int = 4,
    val maxTurns: Int = 12
)

data class GrammarCorrection(
    val userSentence: String,
    val correctedSentence: String,
    val trExplanation: String,
    val naturalAlternative: String,
    val category: String = "Grammar"
)

data class StructuredTurnResult(
    val coachResponseText: String,
    val completedGoalIds: List<String> = emptyList(),
    val newComplication: String? = null,
    val feedbackCorrection: GrammarCorrection? = null,
    val targetWordsIdentified: List<String> = emptyList(),
    val endSessionDecision: Boolean = false,
    val nextSuggestedPrompt: String? = null
)

enum class CorrectionMode {
    FLOW,      // Feedback shown only at end
    BALANCED,  // Short inline feedback cards
    COACH      // Immediate explanation & practice
}

enum class CoachTutor(val displayName: String, val title: String, val persona: String) {
    MAYA(
        displayName = "Maya",
        title = "Conversational & Encouraging",
        persona = "Warm, empathetic, natural American English tutor focusing on confidence, expressive vocabulary, and friendly dialogue."
    ),
    LEO(
        displayName = "Leo",
        title = "Structured & Professional",
        persona = "Direct, articulate, professional business English coach specializing in structured reasoning, concise phrasing, and interview readiness."
    )
}

enum class ConversationViewMode {
    IMMERSIVE, // Large avatar, voice wave, clean call controls
    HYBRID,    // Avatar header + scrollable chat transcript
    CHAT_ONLY  // Full text conversation
}

enum class VoiceState {
    IDLE,
    LISTENING,
    TRANSCRIBING,
    THINKING,
    SYNTHESIZING,
    SPEAKING
}

data class UserProfile(
    val name: String = "Alex",
    val nativeLang: String = "Turkish",
    val englishLevel: String = "B1",
    val purpose: String = "Career & Travel",
    val dailyGoalMins: Int = 10,
    val interests: List<String> = listOf("Technology", "Travel", "Dining"),
    val correctionMode: CorrectionMode = CorrectionMode.BALANCED,
    val selectedCoach: CoachTutor = CoachTutor.MAYA,
    val preferredViewMode: ConversationViewMode = ConversationViewMode.HYBRID,
    val preferredSpeechRate: String = "NORMAL"
)
