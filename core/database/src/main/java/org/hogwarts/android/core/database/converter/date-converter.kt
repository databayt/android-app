package org.hogwarts.android.core.database.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Room TypeConverters for date/time types.
 */
class DateConverter {

    // Instant converters
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(epochMilli: Long?): Instant? = epochMilli?.let { Instant.ofEpochMilli(it) }

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(secondOfDay: Int?): LocalTime? = secondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(epochMilli: Long?): LocalDateTime? =
        epochMilli?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
}
