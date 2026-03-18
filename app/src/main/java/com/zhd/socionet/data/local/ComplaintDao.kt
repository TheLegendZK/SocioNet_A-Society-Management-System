package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getComplaintsForFlat(flatId: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    suspend fun getAllComplaints(): List<ComplaintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)
}
