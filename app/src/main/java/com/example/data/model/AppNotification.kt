package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val branch: String,
    val paperId: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
