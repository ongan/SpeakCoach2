package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoachTutor
import com.example.data.model.CorrectionMode
import com.example.data.model.UserProfile
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.CoachAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyContainer

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (UserProfile) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedLevel by remember { mutableStateOf("B1") }
    var selectedPurpose by remember { mutableStateOf("Career & Global Work") }
    var selectedCoach by remember { mutableStateOf(CoachTutor.MAYA) }
    var selectedCorrectionMode by remember { mutableStateOf(CorrectionMode.BALANCED) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SpeakCoach",
                    color = ActiveCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Step $step of 3",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // Step Content
            when (step) {
                1 -> StepLevelSelection(
                    selectedLevel = selectedLevel,
                    onSelectLevel = { selectedLevel = it },
                    selectedPurpose = selectedPurpose,
                    onSelectPurpose = { selectedPurpose = it }
                )
                2 -> StepCoachSelection(
                    selectedCoach = selectedCoach,
                    onSelectCoach = { selectedCoach = it }
                )
                3 -> StepCorrectionPreference(
                    selectedMode = selectedCorrectionMode,
                    onSelectMode = { selectedCorrectionMode = it }
                )
            }

            // Bottom Navigation Action Button
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        onCompleteOnboarding(
                            UserProfile(
                                englishLevel = selectedLevel,
                                purpose = selectedPurpose,
                                selectedCoach = selectedCoach,
                                correctionMode = selectedCorrectionMode
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ActiveCyan, contentColor = DeepNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (step < 3) "Continue" else "Start Speaking Practice",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun StepLevelSelection(
    selectedLevel: String,
    onSelectLevel: (String) -> Unit,
    selectedPurpose: String,
    onSelectPurpose: (String) -> Unit
) {
    Column {
        Text(
            text = "Welcome to SpeakCoach",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "What is your target English level and primary goal?",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "SELECT YOUR LEVEL", color = ActiveCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        val levels = listOf("A2 - Beginner", "B1 - Intermediate", "B2 - Upper Intermediate", "C1 - Advanced")
        levels.forEach { level ->
            val code = level.take(2)
            val isSelected = selectedLevel == code
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelected) ActiveCyan.copy(alpha = 0.2f) else DeepNavyContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) ActiveCyan else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectLevel(code) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = level, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ActiveCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "YOUR GOAL", color = ActiveCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        val goals = listOf("Career & Global Work", "Travel & Socializing", "Exams & Interviews")
        goals.forEach { goal ->
            val isSelected = selectedPurpose == goal
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelected) ActiveCyan.copy(alpha = 0.2f) else DeepNavyContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) ActiveCyan else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectPurpose(goal) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = goal, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun StepCoachSelection(
    selectedCoach: CoachTutor,
    onSelectCoach: (CoachTutor) -> Unit
) {
    Column {
        Text(
            text = "Choose Your AI Coach",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Switch between Maya and Leo anytime during your practice sessions.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        CoachTutor.values().forEach { coach ->
            val isSelected = selectedCoach == coach
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) ActiveCyan.copy(alpha = 0.15f) else DeepNavyContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) ActiveCyan else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectCoach(coach) }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (coach == CoachTutor.MAYA) Color(0xFF818CF8) else Color(0xFF38BDF8),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = coach.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = coach.title, color = ActiveCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = coach.persona, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCorrectionPreference(
    selectedMode: CorrectionMode,
    onSelectMode: (CorrectionMode) -> Unit
) {
    Column {
        Text(
            text = "Correction Intensity",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "How would you like Maya & Leo to deliver grammar feedback?",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        val options = listOf(
            CorrectionMode.BALANCED to ("Balanced (Recommended)" to "Receive short, helpful feedback cards for notable grammar slips after your turn without interrupting conversation flow."),
            CorrectionMode.FLOW to ("Flow First" to "Zero interruptions during speech. Full grammar analysis reserved for post-session summary report."),
            CorrectionMode.COACH to ("Coach Intensive" to "Detailed immediate corrections and native phrasing suggestions after every response.")
        )

        options.forEach { (mode, pair) ->
            val (title, description) = pair
            val isSelected = selectedMode == mode
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) ActiveCyan.copy(alpha = 0.15f) else DeepNavyContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) ActiveCyan else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectMode(mode) }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}
