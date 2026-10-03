package com.github.vorratshaltung.data.local.converters

import androidx.room.TypeConverter
import com.github.vorratshaltung.data.model.StorageType
import com.github.vorratshaltung.data.model.UnitType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromStorageType(type: StorageType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toStorageType(value: String?): StorageType? {
        return value?.let { StorageType.valueOf(it) }
    }

    @TypeConverter
    fun fromUnitType(type: UnitType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toUnitType(value: String?): UnitType? {
        return value?.let { UnitType.valueOf(it) }
    }
}
