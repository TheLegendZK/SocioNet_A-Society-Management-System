package com.zhd.socionet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NoticeEntity::class,
        ComplaintEntity::class,
        GateEntryEntity::class,
        StaffEntity::class,
        BillEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(SocioNetTypeConverters::class)
abstract class SocioNetDatabase : RoomDatabase() {
    abstract fun noticeDao(): NoticeDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun gateEntryDao(): GateEntryDao
    abstract fun staffDao(): StaffDao
    abstract fun billDao(): BillDao
}
