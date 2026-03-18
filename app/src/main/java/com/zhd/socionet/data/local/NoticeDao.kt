package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    suspend fun getAllNotices(): List<NoticeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices")
    suspend fun clearNotices()
}
