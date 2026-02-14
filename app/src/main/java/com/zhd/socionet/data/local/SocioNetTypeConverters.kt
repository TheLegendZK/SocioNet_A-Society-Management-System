package com.zhd.socionet.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

// --- 1. TYPE CONVERTERS ---
// Handles converting complex objects (Lists, Timestamps) to types Room can store (String, Long)
class SocioNetTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.seconds?.times(1000)
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            Gson().fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// --- 2. ENTITIES (The Tables) ---

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val priority: String,
    val targetAudience: String,
    val postedByAdminId: String,
    val timestamp: Long // Stored as Long in Room
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val flatId: String,
    val createdAt: Long
)

@Entity(tableName = "gate_entries")
data class GateEntryEntity(
    @PrimaryKey val id: String,
    val visitorName: String,
    val visitorType: String,
    val hostFlatId: String,
    val status: String,
    val entryCode: String,
    val entryTime: Long,
    val isStaffEntry: Boolean,
    val linkedStaffId: String?
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val societyId: String,
    val employerFlatIds: List<String> // Handled by TypeConverter
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val flatId: String,
    val month: String,
    val amountDue: Double,
    val isPaid: Boolean,
    val billDate: Long,
    val dueDate: Long
)

// --- 3. DAOs (Data Access Objects) ---

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    suspend fun getAllNotices(): List<NoticeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices")
    suspend fun clearNotices()
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getComplaintsForFlat(flatId: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    suspend fun getAllComplaints(): List<ComplaintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)
}

@Dao
interface GateEntryDao {
    @Query("SELECT * FROM gate_entries WHERE hostFlatId = :flatId ORDER BY entryTime DESC")
    suspend fun getEntriesForFlat(flatId: String): List<GateEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<GateEntryEntity>)

    @Query("DELETE FROM gate_entries WHERE hostFlatId = :flatId")
    suspend fun clearEntriesForFlat(flatId: String)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE societyId = :societyId")
    suspend fun getStaffForSociety(societyId: String): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: List<StaffEntity>)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE flatId = :flatId ORDER BY billDate DESC")
    suspend fun getBillsForFlat(flatId: String): List<BillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>)
}

// --- 4. DATABASE CLASS ---

@Database(
    entities = [
        NoticeEntity::class,
        ComplaintEntity::class,
        GateEntryEntity::class,
        StaffEntity::class,
        BillEntity::class
    ],
    version = 1,
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