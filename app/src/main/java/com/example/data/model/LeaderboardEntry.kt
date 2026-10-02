package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey
    val rollNo: String,
    val studentName: String,
    val branch: String,
    val semester: Int,
    val points: Int,
    val uploadsCount: Int,
    val averagePaperRating: Float,
    val badgeTitle: String,
    val rank: Int = 0
)
