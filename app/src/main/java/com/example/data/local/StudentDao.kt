package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_profiles WHERE isCurrentActiveUser = 1 LIMIT 1")
    fun getActiveProfile(): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profiles WHERE rollNo = :rollNo LIMIT 1")
    suspend fun getProfileByRollNo(rollNo: String): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfile)

    @Query("UPDATE student_profiles SET isCurrentActiveUser = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE student_profiles SET isCurrentActiveUser = 1 WHERE rollNo = :rollNo")
    suspend fun setActiveProfile(rollNo: String)

    @Query("UPDATE student_profiles SET contributionPoints = contributionPoints + :points, uploadsCount = uploadsCount + 1 WHERE rollNo = :rollNo")
    suspend fun incrementContribution(rollNo: String, points: Int)

    @Query("UPDATE student_profiles SET downloadsCount = downloadsCount + 1 WHERE rollNo = :rollNo")
    suspend fun incrementDownloads(rollNo: String)
}
