package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperDao {
    @Query("SELECT * FROM question_papers ORDER BY uploadDateMillis DESC")
    fun getAllPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM question_papers WHERE id = :id LIMIT 1")
    fun getPaperById(id: Long): Flow<PaperEntity?>

    @Query("SELECT * FROM question_papers WHERE isDownloaded = 1 ORDER BY downloadedTimestamp DESC")
    fun getDownloadedPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM question_papers WHERE isBookmarked = 1 ORDER BY uploadDateMillis DESC")
    fun getBookmarkedPapers(): Flow<List<PaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(papers: List<PaperEntity>)

    @Update
    suspend fun updatePaper(paper: PaperEntity)

    @Query("UPDATE question_papers SET isDownloaded = :isDownloaded, downloadedTimestamp = :timestamp, downloadCount = downloadCount + 1 WHERE id = :id")
    suspend fun updateDownloadStatus(id: Long, isDownloaded: Boolean, timestamp: Long)

    @Query("UPDATE question_papers SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Long, isBookmarked: Boolean)

    @Query("UPDATE question_papers SET averageRating = :avgRating, ratingCount = :count WHERE id = :id")
    suspend fun updateRating(id: Long, avgRating: Float, count: Int)

    @Query("SELECT COUNT(*) FROM question_papers")
    suspend fun getPaperCount(): Int

    @Query("DELETE FROM question_papers")
    suspend fun deleteAllPapers()
}
