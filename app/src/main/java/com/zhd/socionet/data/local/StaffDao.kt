package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE societyId = :societyId")
    suspend fun getStaffForSociety(societyId: String): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: List<StaffEntity>)
}
