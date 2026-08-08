package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GoalProgressEntity
import com.example.data.db.ScenarioTurnEntity
import com.example.data.model.ConversationViewMode
import com.example.data.model.GrammarCorrection
import com.example.data.model.VoiceState
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.CoachAvatarGraphic
import com.example.ui.components.VoiceStateIndicator
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.CoachAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyContainer
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.CoachViewModel

@Composable
fun CoachScreen(
    viewModel: CoachViewModel,
    onNavigateSummary: (String) -> Unit
) {
    val activeScenario by viewModel.activeScenario.collectAsState()
    val turns by viewModel.turns.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val viewMode by viewModel.currentViewMode.collectAsState()
    val coach by viewModel.selectedCoach.collectAsState()
    val latestResult by viewModel.latestFeedback.collectAsState()
    val subtitlesEnabled by viewModel.subtitlesEnabled.collectAsState()

    val partialSpeechText by viewModel.speechRecognizer.partialText.collectAsState()
    val audioLevel by viewModel.speechRecognizer.audioLevel.collectAsState()
    val isListening by viewModel.speechRecognizer.isListening.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest turn
    LaunchedEffect(turns.size) {
        if (turns.isNotEmpty()) {
            listState.animateScrollToItem(turns.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top Control Bar: Scenario Title, Tutor Switcher, View Switcher, End Call
            TopControlBar(
                title = activeScenario?.title ?: "Speaking Session",
                viewMode = viewMode,
                coachName = coach.displayName,
                onToggleCoach = { viewModel.toggleCoachTutor() },
                onSelectViewMode = { viewModel.setViewMode(it) },
                onEndSession = { viewModel.endSessionAndNavigateSummary(onNavigateSummary) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Goals Progress Pills
            GoalsHeaderRow(goals = goals)

            Spacer(modifier = Modifier.height(8.dp))

            // Main View Content based on View Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (viewMode) {
                    ConversationViewMode.IMMERSIVE -> {
                        ImmersiveCallView(
                            coach = coach,
                            voiceState = voiceState,
                            audioLevel = audioLevel,
                            isListening = isListening,
                            latestTurn = turns.lastOrNull(),
                            subtitlesEnabled = subtitlesEnabled,
                            partialSpeech = partialSpeechText
                        )
                    }
                    ConversationViewMode.HYBRID -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Avatar Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CoachAvatarGraphic(coach = coach, voiceState = voiceState, size = 80.dp)
                            }

                            VoiceStateIndicator(voiceState = voiceState)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Transcript List
                            TranscriptList(
                                turns = turns,
                                latestCorrection = latestResult?.feedbackCorrection,
                                listState = listState,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    ConversationViewMode.CHAT_ONLY -> {
                        TranscriptList(
                            turns = turns,
                            latestCorrection = latestResult?.feedbackCorrection,
                            listState = listState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Audio Waveform
            if (isListening) {
                AudioWaveformVisualizer(
                    audioLevel = audioLevel,
                    isListening = isListening,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Bottom Interactive Mic & Text Input Control Bar
            BottomSpeechControlBar(
                voiceState = voiceState,
                isListening = isListening,
                partialText = partialSpeechText,
                subtitlesEnabled = subtitlesEnabled,
                textInput = textInput,
                onTextInputChange = { textInput = it },
                onSendText = {
                    viewModel.processUserTextInput(textInput)
                    textInput = ""
                },
                onStartListening = { viewModel.startListening() },
                onStopListening = { viewModel.stopListeningAndSubmit() },
                onToggleSubtitles = { viewModel.toggleSubtitles() }
            )
        }
    }
}

@Composable
private fun TopControlBar(
    title: String,
    viewMode: ConversationViewMode,
    coachName: String,
    onToggleCoach: () -> Unit,
    onSelectViewMode: (ConversationViewMode) -> Unit,
    onEndSession: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepNavyContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleCoach() }
                    ) {
                        Text(
                            text = "Coach: $coachName",
                            color = ActiveCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Switch Coach", tint = ActiveCyan, modifier = Modifier.size(14.dp))
                    }
                }

                // End Session Call Button
                IconButton(
                    onClick = onEndSession,
                    modifier = Modifier
                        .background(Color(0xFFEF4444), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View Switcher Segmented Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavy, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConversationViewMode.values().forEach { mode ->
                    val isSelected = viewMode == mode
                    val label = when (mode) {
                        ConversationViewMode.IMMERSIVE -> "Call Mode"
                        ConversationViewMode.HYBRID -> "Hybrid View"
                        ConversationViewMode.CHAT_ONLY -> "Chat Only"
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ActiveCyan else Color.Transparent)
                            .clickable { onSelectViewMode(mode) }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) DeepNavy else Color.White.copy(alpha = 0.8f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsHeaderRow(goals: List<GoalProgressEntity>) {
    LazyColumn(modifier = Modifier.fillMaxWidth().height(32.dp)) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                goals.forEach { goal ->
                    Surface(
                        color = if (goal.isCompleted) SuccessEmerald.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        contentColor = if (goal.isCompleted) SuccessEmerald else Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (goal.isCompleted) Icons.Default.Check else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = goal.goalText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImmersiveCallView(
    coach: com.example.data.model.CoachTutor,
    voiceState: VoiceState,
    audioLevel: Float,
    isListening: Boolean,
    latestTurn: ScenarioTurnEntity?,
    subtitlesEnabled: Boolean,
    partialSpeech: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        CoachAvatarGraphic(coach = coach, voiceState = voiceState, size = 160.dp)

        VoiceStateIndicator(voiceState = voiceState)

        // Live Subtitles Banner
        if (subtitlesEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepNavyContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isListening && partialSpeech.isNotBlank()) "\"$partialSpeech...\"" else latestTurn?.text ?: "Listening for your response...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TranscriptList(
    turns: List<ScenarioTurnEntity>,
    latestCorrection: GrammarCorrection?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(turns) { turn ->
            val isUser = turn.speaker == "USER"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    color = if (isUser) ActiveCyan else DeepNavyContainer,
                    contentColor = if (isUser) DeepNavy else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isUser) "You" else "Maya",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUser) DeepNavy.copy(alpha = 0.7f) else ActiveCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = turn.text,
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )
                    }
                }
            }
        }

        // Inline Feedback Card if available
        if (latestCorrection != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CoachAmber.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoachAmber),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CoachAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Natural Phrasing Tip", color = CoachAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Better: \"${latestCorrection.correctedSentence}\"", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = latestCorrection.trExplanation, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSpeechControlBar(
    voiceState: VoiceState,
    isListening: Boolean,
    partialText: String,
    subtitlesEnabled: Boolean,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    onSendText: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onToggleSubtitles: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepNavyContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Subtitle Toggle
                IconButton(onClick = onToggleSubtitles) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Toggle Subtitles",
                        tint = if (subtitlesEnabled) ActiveCyan else Color.Gray
                    )
                }

                // Main Pulsing Mic Button (Tap or Hold)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFEF4444) else ActiveCyan)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onStartListening()
                                    tryAwaitRelease()
                                    onStopListening()
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = "Hold or Tap to Speak",
                        tint = DeepNavy,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Text Input Toggle / Quick Send
                IconButton(onClick = { if (textInput.isNotBlank()) onSendText() }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Text",
                        tint = if (textInput.isNotBlank()) ActiveCyan else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text Input Field for Hybrid Typing
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextInputChange,
                placeholder = { Text("Or type your response here...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ActiveCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )
        }
    }
}
