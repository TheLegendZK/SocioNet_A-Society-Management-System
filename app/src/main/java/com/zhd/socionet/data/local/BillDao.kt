package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE flatId = :flatId ORDER BY billDate DESC")
    suspend fun getBillsForFlat(flatId: String): List<BillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>)
}
