package eka.care.records.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eka.care.records.client.model.RecordUiState
import eka.care.records.data.entity.RecordStatus
import eka.care.records.data.remote.dto.response.SmartReport

class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if(value == null) return null else Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if(value == null)
            return null
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromJson(value: String?): SmartReport? {
        return if (value == null) null else Gson().fromJson(value, SmartReport::class.java)
    }

    @TypeConverter
    fun toJson(smartReport: SmartReport?): String? {
        return if (smartReport == null) null else Gson().toJson(smartReport)
    }

    @TypeConverter
    fun fromRecordStatus(status: RecordStatus): Int = status.value

    @TypeConverter
    fun toRecordStatus(value: Int): RecordStatus =
        RecordStatus.entries.find { it.value == value } ?: RecordStatus.NONE

    @TypeConverter
    fun fromRecordState(state: RecordUiState): Int = state.status

    @TypeConverter
    fun toRecordState(value: Int): RecordUiState =
        RecordUiState.entries.find { it.status == value } ?: RecordUiState.NONE
}