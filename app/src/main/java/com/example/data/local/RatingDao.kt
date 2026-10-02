package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaperRating
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT * FROM paper_ratings WHERE paperId = :paperId ORDER BY timestampMillis DESC")
    fun getRatingsForPaper(paperId: Long): Flow<List<PaperRating>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: PaperRating): Long

    @Query("SELECT AVG(rating) FROM paper_ratings WHERE paperId = :paperId")
    suspend fun getAverageRating(paperId: Long): Float?

    @Query("SELECT COUNT(*) FROM paper_ratings WHERE paperId = :paperId")
    suspend fun getRatingCount(paperId: Long): Int
}
