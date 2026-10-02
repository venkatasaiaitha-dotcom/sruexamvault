package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paper_ratings")
data class PaperRating(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paperId: Long,
    val raterRollNo: String,
    val raterName: String,
    val rating: Int, // 1 to 5
    val tag: String = "Clear Questions", // e.g., "Exact Syllabus", "Answer Key Included", "Good Scan"
    val reviewComment: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
