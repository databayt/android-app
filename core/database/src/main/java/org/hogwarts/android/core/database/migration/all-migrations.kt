package org.hogwarts.android.core.database.migration

import androidx.room.migration.Migration

/**
 * Every authored migration, oldest first. v1→v7 never shipped outside
 * development, so the chain starts at 7. A schema bump adds its
 * `MIGRATION_X_Y` here; the database module and the migration test both
 * read this list, so the two cannot drift.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15,
    MIGRATION_15_16,
    MIGRATION_16_17,
    MIGRATION_17_18,
    MIGRATION_18_19,
    MIGRATION_19_20,
    MIGRATION_20_21,
    MIGRATION_21_22,
    MIGRATION_22_23,
    MIGRATION_23_24,
)
