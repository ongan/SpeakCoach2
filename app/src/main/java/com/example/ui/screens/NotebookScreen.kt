package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.CoachAmber
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SoftBackground
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.NotebookViewModel

@Composable
fun NotebookScreen(
    viewModel: NotebookViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Flashcard Review", "Saved Words", "Grammar Corrections")

    val reviewQueue by viewModel.reviewQueue.collectAsState()
    val currentIndex by viewModel.currentReviewIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newWordText by remember { mutableStateOf("") }
    var newDefText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Learner Notebook & SRS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Review saved vocabulary and grammar corrections",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DeepNavy
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> FlashcardReviewTab(
                items = reviewQueue,
                currentIndex = currentIndex,
                isFlipped = isFlipped,
                onFlip = { viewModel.flipCard() },
                onSubmitResult = { remembered -> viewModel.submitReviewResult(remembered) }
            )
            1 -> SavedWordsListTab(
                items = reviewQueue,
                onAddClick = { showAddDialog = true }
            )
            2 -> CorrectionsListTab()
        }
    }
}

@Composable
private fun FlashcardReviewTab(
    items: List<com.example.data.db.VocabularyItemEntity>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSubmitResult: (Boolean) -> Unit
) {
    val currentItem = items.getOrNull(currentIndex)

    if (currentItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Review queue complete for today! 🎉", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Card ${currentIndex + 1} of ${items.size}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Interactive Flip Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepNavy),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clickable { onFlip() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isFlipped) {
                    Text(
                        text = currentItem.word,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentItem.phonetic,
                        color = ActiveCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Tap to reveal definition & example",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                } else {
                    Surface(
                        color = ActiveCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentItem.partOfSpeech,
                            color = ActiveCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentItem.definition,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"${currentItem.exampleSentence}\"",
                        color = CoachAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Action Response Buttons (Forgot vs Remembered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onSubmitResult(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Forgot", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { onSubmitResult(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Remembered", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SavedWordsListTab(
    items: List<com.example.data.db.VocabularyItemEntity>,
    onAddClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { vocab ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = vocab.word, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = vocab.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = vocab.definition, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "\"${vocab.exampleSentence}\"", fontSize = 13.sp, color = CoachAmber, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun CorrectionsListTab() {
    val sampleCorrections = listOf(
        "I want oat milk." to ("I'd like oat milk, please." to "'I'd like' kibar restoran kalıbıdır."),
        "I am agree with you." to ("I agree with you." to "'Agree' fiil olduğu için 'am' eklenmez."),
        "He don't know." to ("He doesn't know." to "Üçüncü tekil şahısta 'doesn't' kullanılır.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sampleCorrections) { (original, pair) ->
            val (corrected, note) = pair
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Original: \"$original\"", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Better: \"$corrected\"", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SuccessEmerald)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
