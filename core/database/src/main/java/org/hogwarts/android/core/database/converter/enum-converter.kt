package org.hogwarts.android.core.database.converter

import androidx.room.TypeConverter
import org.hogwarts.android.core.database.entity.OperationStatus
import org.hogwarts.android.core.database.entity.OperationType

/**
 * Room TypeConverters for enum types.
 */
class EnumConverter {

    // OperationType converters
    @TypeConverter
    fun fromOperationType(type: OperationType): String = type.name

    @TypeConverter
    fun toOperationType(value: String): OperationType = OperationType.valueOf(value)

    // OperationStatus converters
    @TypeConverter
    fun fromOperationStatus(status: OperationStatus): String = status.name

    @TypeConverter
    fun toOperationStatus(value: String): OperationStatus = OperationStatus.valueOf(value)
}
