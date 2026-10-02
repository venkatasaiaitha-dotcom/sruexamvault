package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_papers")
data class PaperEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectCode: String,
    val subjectName: String,
    val branch: String,
    val semester: Int,
    val examType: String,
    val academicYear: String,
    val examMonthYear: String,
    val regulation: String = "R22",
    val maxMarks: Int = 60,
    val duration: String = "3 Hours",
    val instructions: String = "Answer ALL questions in Part-A. Answer any FIVE questions from Part-B.",
    val partAContent: String,
    val partBContent: String,
    val solutionHints: String = "",
    val uploaderName: String,
    val uploaderRollNo: String,
    val uploaderEmail: String,
    val uploadDateMillis: Long = System.currentTimeMillis(),
    val averageRating: Float = 4.5f,
    val ratingCount: Int = 0,
    val downloadCount: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadedTimestamp: Long = 0L,
    val isBookmarked: Boolean = false
)
