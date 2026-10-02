package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey
    val rollNo: String,
    val fullName: String,
    val email: String,
    val branch: String,
    val semester: Int,
    val contributionPoints: Int = 100,
    val uploadsCount: Int = 0,
    val downloadsCount: Int = 0,
    val rankBadge: String = "Rising Scholar",
    val isCurrentActiveUser: Boolean = false
)
