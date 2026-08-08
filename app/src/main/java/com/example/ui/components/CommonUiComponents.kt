package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoachTutor
import com.example.data.model.VoiceState
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.CoachAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyContainer
import com.example.ui.theme.SuccessEmerald

@Composable
fun CefrBadge(cefr: String, modifier: Modifier = Modifier) {
    Surface(
        color = when (cefr.uppercase()) {
            "A1", "A2" -> Color(0xFFE9EDC9)
            "B1", "B2" -> Color(0xFFFEFAE0)
            else -> Color(0xFFCCD5AE)
        },
        contentColor = Color(0xFF2D3128),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5A58D).copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = cefr,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CoachAvatarGraphic(
    coach: CoachTutor,
    voiceState: VoiceState,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val avatarBg = if (coach == CoachTutor.MAYA) {
        Brush.linearGradient(listOf(Color(0xFF6B705C), Color(0xFFA5A58D)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF5A7052), Color(0xFF8F9E8B)))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size * 1.3f)
    ) {
        // Glowing animated ring
        if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(size * pulseScale)
                    .clip(CircleShape)
                    .background(if (voiceState == VoiceState.LISTENING) ActiveCyan.copy(alpha = 0.3f) else CoachAmber.copy(alpha = 0.3f))
            )
        }

        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(size)
                .border(
                    width = 3.dp,
                    color = if (voiceState == VoiceState.SPEAKING) CoachAmber else ActiveCyan,
                    shape = CircleShape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(avatarBg)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = coach.displayName,
                        tint = Color(0xFFFEFAE0),
                        modifier = Modifier.size(size * 0.45f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coach.displayName,
                        color = Color(0xFFFEFAE0),
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.12f).sp
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    audioLevel: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val barCount = 24
        val barWidth = size.width / (barCount * 1.8f)
        val middleY = size.height / 2f

        for (i in 0 until barCount) {
            val factor = if (isListening) (audioLevel + (i % 5) * 0.15f + waveOffset * 0.2f).coerceIn(0.1f, 1f) else 0.15f
            val barHeight = (size.height * factor).coerceAtLeast(6.dp.toPx())
            val x = i * (barWidth * 1.8f) + barWidth

            drawRoundRect(
                color = if (isListening) ActiveCyan else Color.Gray.copy(alpha = 0.4f),
                topLeft = androidx.compose.ui.geometry.Offset(x, middleY - barHeight / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
            )
        }
    }
}

@Composable
fun VoiceStateIndicator(voiceState: VoiceState) {
    val (label, color) = when (voiceState) {
        VoiceState.IDLE -> "Ready • Tap to Speak" to ActiveCyan
        VoiceState.LISTENING -> "Listening..." to ActiveCyan
        VoiceState.TRANSCRIBING -> "Processing Speech..." to CoachAmber
        VoiceState.THINKING -> "Maya is Thinking..." to CoachAmber
        VoiceState.SYNTHESIZING -> "Preparing Audio..." to CoachAmber
        VoiceState.SPEAKING -> "Maya is Speaking..." to SuccessEmerald
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
