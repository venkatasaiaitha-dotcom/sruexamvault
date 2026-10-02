package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppNotification
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PaperEntity
import com.example.data.model.PaperRating
import com.example.data.model.StudentProfile

@Database(
    entities = [
        PaperEntity::class,
        StudentProfile::class,
        LeaderboardEntry::class,
        PaperRating::class,
        AppNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paperDao(): PaperDao
    abstract fun studentDao(): StudentDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun ratingDao(): RatingDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sru_exam_vault.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
