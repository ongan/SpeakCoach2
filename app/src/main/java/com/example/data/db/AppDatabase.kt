package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        ScenarioSessionEntity::class,
        ScenarioTurnEntity::class,
        GoalProgressEntity::class,
        CorrectionEntity::class,
        VocabularyItemEntity::class,
        DailyActivityEntity::class,
        UserFactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun scenarioSessionDao(): ScenarioSessionDao
    abstract fun correctionDao(): CorrectionDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun userFactDao(): UserFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speakcoach_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
