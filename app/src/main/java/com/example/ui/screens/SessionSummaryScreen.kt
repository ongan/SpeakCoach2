package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.CoachAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyContainer
import com.example.ui.theme.SoftBackground
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.CoachViewModel

@Composable
fun SessionSummaryScreen(
    viewModel: CoachViewModel,
    sessionId: String,
    onDone: () -> Unit
) {
    val activeScenario by viewModel.activeScenario.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val turns by viewModel.turns.collectAsState()

    val completedGoalsCount = goals.count { it.isCompleted }
    val userTurnsCount = turns.count { it.speaker == "USER" }
    val spokenWordsCount = turns.filter { it.speaker == "USER" }.sumOf { it.text.split(" ").size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Celebration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CoachAmber.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = CoachAmber, modifier = Modifier.size(36.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Session Completed!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = activeScenario?.title ?: "Ordering at a Busy Cafe",
                        color = ActiveCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Evidence Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatPill(title = "Sub-Goals Met", value = "$completedGoalsCount / ${goals.size}")
                        StatPill(title = "Turns Spoken", value = "$userTurnsCount")
                        StatPill(title = "Words Uttered", value = "$spokenWordsCount")
                    }
                }
            }
        }

        // Sub-Goal Outcomes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Goal Completion Checklist",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    goals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (goal.isCompleted) SuccessEmerald else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = goal.goalText,
                                fontSize = 14.sp,
                                fontWeight = if (goal.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Key Grammar Spotlight
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CoachAmber.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoachAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CoachAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Grammar Upgrade",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoachAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Original: \"I want oat milk please.\"", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Native Alternative: \"I'd like oat milk, if you have it.\"", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Anlaşılan kibar isteklerde 'I'd like' kalıbını kullanmak diyaloglarda özgüven sağlar.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Action Button
        item {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Back to Practice Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = ActiveCyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = title, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}
