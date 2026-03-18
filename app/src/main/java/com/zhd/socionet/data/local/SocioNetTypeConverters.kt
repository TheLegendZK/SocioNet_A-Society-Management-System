package com.zhd.socionet.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

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
