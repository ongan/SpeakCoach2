package com.example.data.model

object ScenarioCatalogRepository {

    val defaultScenarios = listOf(
        Scenario(
            id = "cafe_ordering",
            title = "Ordering at a Busy Cafe",
            description = "Practice ordering coffee, specifying milk alternatives, handling noise, and confirming your total.",
            category = "Daily Life",
            cefr = "A2",
            durationMins = 5,
            iconName = "cafe",
            accentHex = "#00E3FD",
            primaryGoal = "Successfully order your coffee with custom preferences and pay.",
            subGoals = listOf(
                Goal("goal_greet_order", "Greet the barista and state your coffee order clearly", isMandatory = true),
                Goal("goal_specify_milk", "Ask for a specific milk preference (e.g., oat milk or almond milk)", isMandatory = true),
                Goal("goal_confirm_price", "Ask or confirm the total price and payment method", isMandatory = true),
                Goal("goal_handle_complication", "Politely resolve any complication (e.g., out of oat milk or wrong size)", isMandatory = false)
            ),
            userRole = "Customer ordering coffee during morning rush hour",
            aiRole = "Friendly but fast-paced Barista at 'Urban Brew Cafe'",
            aiPersonality = "Upbeat, slightly hurried, polite, attentive to order details",
            openers = listOf(
                "Good morning! Welcome to Urban Brew. What can I get started for you today?",
                "Hi there! Line is moving fast today—what are you in the mood for?",
                "Hey! Welcome in. Are you having your drink here or taking it to go?"
            ),
            complications = listOf(
                "We just ran out of oat milk! We have almond, soy, or dairy milk left. Which would you prefer?",
                "Sorry, it's a bit noisy in here! Did you say a medium or a large?",
                "Our card reader is running a bit slow today. Would you like a physical receipt printed?"
            ),
            minTurns = 4,
            maxTurns = 10
        ),
        Scenario(
            id = "hotel_checkin",
            title = "Checking In at a Boutique Hotel",
            description = "Navigate front desk check-in, ask about wifi and breakfast, and politely request a room upgrade or quiet floor.",
            category = "Travel",
            cefr = "B1",
            durationMins = 7,
            iconName = "hotel",
            accentHex = "#FFBA38",
            primaryGoal = "Complete check-in, confirm reservation details, and clarify hotel amenities.",
            subGoals = listOf(
                Goal("goal_provide_name", "State your reservation name and booking confirmation", isMandatory = true),
                Goal("goal_ask_breakfast", "Inquire about breakfast hours and WiFi password", isMandatory = true),
                Goal("goal_request_room_pref", "Politely request a quiet room or higher floor view", isMandatory = false),
                Goal("goal_confirm_checkout", "Confirm checkout time and luggage storage options", isMandatory = true)
            ),
            userRole = "Traveler arriving at the hotel after a long flight",
            aiRole = "Front Desk Manager at 'The Grand Horizon Hotel'",
            aiPersonality = "Professional, welcoming, courteous, customer-service oriented",
            openers = listOf(
                "Welcome to The Grand Horizon! How may I assist you this afternoon?",
                "Good evening! Checking in with us today?",
                "Hello! Welcome. May I have the name on your reservation, please?"
            ),
            complications = listOf(
                "I see your reservation, but the room won't be ready for another 20 minutes. May I hold your bags while you wait?",
                "Your reservation is on our 3rd floor near the elevator. Would you prefer a room further down the hallway for less noise?",
                "Could I please see a government-issued photo ID and credit card for incidentals?"
            ),
            minTurns = 4,
            maxTurns = 10
        ),
        Scenario(
            id = "job_interview",
            title = "Job Interview: Pitching Yourself",
            description = "Practice answering behavioral interview questions, summarizing your background, and asking thoughtful questions to the interviewer.",
            category = "Career",
            cefr = "B2",
            durationMins = 10,
            iconName = "work",
            accentHex = "#10B981",
            primaryGoal = "Deliver a confident pitch, answer a STAR method question, and ask 2 relevant questions.",
            subGoals = listOf(
                Goal("goal_elevator_pitch", "Deliver a 2-sentence summary of your background and passion", isMandatory = true),
                Goal("goal_star_example", "Describe a challenge you solved in a past project", isMandatory = true),
                Goal("goal_ask_interviewer", "Ask the interviewer at least one question about team culture or growth", isMandatory = true),
                Goal("goal_closing_statement", "Express gratitude and state why you're excited for the role", isMandatory = true)
            ),
            userRole = "Job Candidate applying for a Senior Role",
            aiRole = "Hiring Manager at a top tech company",
            aiPersonality = "Insightful, professional, curious, evaluating problem-solving skills",
            openers = listOf(
                "Thanks for joining us today! To kick things off, could you walk me through your background and why you're interested in this role?",
                "Great to meet you! Let's start with a quick overview: what led you to apply for this position?",
                "Welcome! We're excited to learn more about you. Tell me a bit about your current work and key accomplishments."
            ),
            complications = listOf(
                "That sounds interesting! Could you give me a specific example of when that project didn't go as planned?",
                "How do you handle conflicting priorities when two stakeholders demand urgent deliverables at the same time?",
                "What is one technical skill or workflow you're currently working on improving?"
            ),
            minTurns = 5,
            maxTurns = 12
        ),
        Scenario(
            id = "salary_negotiation",
            title = "Negotiating a Raise or Compensation",
            description = "Roleplay a tactful conversation with your manager advocating for your market value and key deliverables.",
            category = "Career",
            cefr = "C1",
            durationMins = 10,
            iconName = "payments",
            accentHex = "#3B82F6",
            primaryGoal = "Present data-backed evidence for a salary adjustment and negotiate professionally.",
            subGoals = listOf(
                Goal("goal_state_reason", "Outline your recent contributions and metrics clearly", isMandatory = true),
                Goal("goal_propose_target", "State your target compensation expectation confidently", isMandatory = true),
                Goal("goal_address_pushback", "Respond calmly to budget constraints or timeline delays", isMandatory = true),
                Goal("goal_agree_next_step", "Establish a clear date for follow-up or review", isMandatory = true)
            ),
            userRole = "High-performing Employee requesting a compensation review",
            aiRole = "Department Manager reviewing annual performance",
            aiPersonality = "Supportive of the employee, but constrained by corporate budget guidelines and performance benchmarks",
            openers = listOf(
                "Thanks for setting up time on my calendar today. You mentioned you wanted to discuss your recent performance review?",
                "Hi! I saw your meeting invite regarding your role and compensation. What did you have in mind?",
                "Good afternoon! I'm glad we could connect. How are you feeling about your workload and recent projects?"
            ),
            complications = listOf(
                "I agree your performance has been great, but our Q3 budget is tight right now. Could we revisit a base increase in 6 months?",
                "The target percentage you mentioned is higher than our standard band. Could we consider a performance bonus tied to Q4 goals instead?",
                "Which specific metric or project from the last quarter do you feel had the biggest business impact?"
            ),
            minTurns = 5,
            maxTurns = 12
        ),
        Scenario(
            id = "watercooler_talk",
            title = "Water Cooler Small Talk with a Colleague",
            description = "Engage in relaxed office small talk about weekend plans, hobbies, and transitioning smoothly into work tasks.",
            category = "Social",
            cefr = "B1",
            durationMins = 5,
            iconName = "groups",
            accentHex = "#8B5CF6",
            primaryGoal = "Maintain friendly rapport, share a personal update, and transition to a work topic.",
            subGoals = listOf(
                Goal("goal_weekend_update", "Share what you did or plan to do over the weekend", isMandatory = true),
                Goal("goal_ask_colleague", "Ask your colleague an engaging follow-up question", isMandatory = true),
                Goal("goal_transition_work", "Smoothly transition the chat to a shared work topic or project", isMandatory = true)
            ),
            userRole = "Team Member chatting in the breakroom",
            aiRole = "Friendly Coworker grabbing coffee",
            aiPersonality = "Casual, warm, humorous, approachable",
            openers = listOf(
                "Hey! Happy Friday! Got any fun plans lined up for the weekend?",
                "Oh hey! How's your morning going so far? Grabbing your second cup of coffee too?",
                "Hi! How was that movie/concert you mentioned last week?"
            ),
            complications = listOf(
                "Oh really? I've been wanting to try that restaurant/hobby! Is it beginner-friendly?",
                "By the way, did you happen to catch the update in the team Slack channel earlier?",
                "Time flies! Are you heading into the 10:00 AM sync as well?"
            ),
            minTurns = 4,
            maxTurns = 8
        ),
        Scenario(
            id = "free_talk",
            title = "Free Talk & Open Conversation",
            description = "Unstructured, spontaneous speaking practice on any topic of your choice with real-time feedback.",
            category = "Social",
            cefr = "A2-C1",
            durationMins = 10,
            iconName = "forum",
            accentHex = "#EC4899",
            primaryGoal = "Speak naturally for 5+ minutes on topics you enjoy.",
            subGoals = listOf(
                Goal("goal_express_thought", "Express your opinion on a topic of interest", isMandatory = true),
                Goal("goal_use_new_vocab", "Try using a descriptive adjective or connector word", isMandatory = false),
                Goal("goal_ask_coach_opinion", "Ask Maya/Leo for their perspective", isMandatory = false)
            ),
            userRole = "English Learner practicing open conversation",
            aiRole = "Your personal AI English Coach",
            aiPersonality = "Engaging, inquisitive, supportive, helpful with natural phrasing",
            openers = listOf(
                "Welcome to Free Talk! What's on your mind today? We can discuss news, movies, technology, or your daily routine!",
                "Hey there! I'm all ears today. What's an interesting thought, event, or hobby you'd like to chat about?",
                "Hi! Today is your open floor. What topic would you love to practice speaking about?"
            ),
            complications = listOf(
                "That's a fascinating perspective! What led you to feel that way about it?",
                "If you could change one thing about that situation, what would it be?",
                "How would you explain that concept to someone who has never heard of it before?"
            ),
            minTurns = 4,
            maxTurns = 15
        )
    )

    fun getById(id: String): Scenario {
        return defaultScenarios.find { it.id == id } ?: defaultScenarios.first()
    }
}
