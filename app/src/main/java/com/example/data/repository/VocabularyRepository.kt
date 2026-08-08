package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.CorrectionEntity
import com.example.data.db.VocabularyItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VocabularyRepository(private val db: AppDatabase) {

    private val vocabDao = db.vocabularyDao()
    private val correctionDao = db.correctionDao()

    val allVocabulary: Flow<List<VocabularyItemEntity>> = vocabDao.getAllVocabulary()
    val allCorrections: Flow<List<CorrectionEntity>> = correctionDao.getAllCorrections()

    fun getReviewQueue(currentTime: Long = System.currentTimeMillis()): Flow<List<VocabularyItemEntity>> =
        vocabDao.getReviewQueue(currentTime)

    suspend fun saveVocabulary(item: VocabularyItemEntity) = withContext(Dispatchers.IO) {
        vocabDao.insertVocabulary(item)
    }

    suspend fun updateReviewResult(word: String, remembered: Boolean) = withContext(Dispatchers.IO) {
        val existing = vocabDao.getWord(word) ?: return@withContext
        val newCount = existing.reviewCount + 1
        val newInterval = if (remembered) (existing.reviewIntervalDays * 2).coerceAtMost(30) else 1
        val newStatus = if (newInterval >= 14) "MASTERED" else "REVIEW"
        val nextTime = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000L)

        vocabDao.updateVocabulary(
            existing.copy(
                reviewCount = newCount,
                reviewIntervalDays = newInterval,
                status = newStatus,
                nextReviewTime = nextTime
            )
        )
    }

    suspend fun seedInitialVocabularyIfEmpty() = withContext(Dispatchers.IO) {
        val currentCount = vocabDao.getWord("Synergize")
        if (currentCount == null) {
            val initialList = listOf(
                VocabularyItemEntity(
                    word = "Synergize",
                    phonetic = "/ˈsɪn.ər.dʒaɪz/",
                    definition = "To combine or work together in order to be more effective.",
                    partOfSpeech = "Verb",
                    exampleSentence = "We need to synergize our marketing efforts with product development.",
                    synonymsCsv = "collaborate, unite, integrate",
                    status = "REVIEW"
                ),
                VocabularyItemEntity(
                    word = "Actionable",
                    phonetic = "/ˈæk.ʃən.ə.bəl/",
                    definition = "Able to be done or acted on; having practical value.",
                    partOfSpeech = "Adjective",
                    exampleSentence = "The report provides actionable insights for improving customer retention.",
                    synonymsCsv = "practical, usable, applicable",
                    status = "NEW"
                ),
                VocabularyItemEntity(
                    word = "Pivot",
                    phonetic = "/ˈpɪv.ət/",
                    definition = "To fundamentally change the direction of a strategy or business.",
                    partOfSpeech = "Verb",
                    exampleSentence = "The startup had to pivot its strategy after initial market feedback.",
                    synonymsCsv = "shift, adjust, redirect",
                    status = "REVIEW"
                ),
                VocabularyItemEntity(
                    word = "Ubiquitous",
                    phonetic = "/juːˈbɪk.wɪ.təs/",
                    definition = "Present, appearing, or found everywhere.",
                    partOfSpeech = "Adjective",
                    exampleSentence = "Smartphones have become ubiquitous in modern daily life.",
                    synonymsCsv = "omnipresent, pervasive, universal",
                    status = "NEW"
                ),
                VocabularyItemEntity(
                    word = "Epitome",
                    phonetic = "/ɪˈpɪt.ə.mi/",
                    definition = "A person or thing that is a perfect example of a quality.",
                    partOfSpeech = "Noun",
                    exampleSentence = "She is the epitome of professional poise and leadership.",
                    synonymsCsv = "embodiment, paragon, exemplar",
                    status = "NEW"
                )
            )
            for (item in initialList) {
                vocabDao.insertVocabulary(item)
            }
        }
    }
}
