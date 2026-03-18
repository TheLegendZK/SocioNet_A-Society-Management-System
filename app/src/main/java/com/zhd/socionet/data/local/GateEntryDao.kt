package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GateEntryDao {
    @Query("SELECT * FROM gate_entries WHERE hostFlatId = :flatId ORDER BY entryTime DESC")
    suspend fun getEntriesForFlat(flatId: String): List<GateEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<GateEntryEntity>)

    @Query("DELETE FROM gate_entries WHERE hostFlatId = :flatId")
    suspend fun clearEntriesForFlat(flatId: String)
}
