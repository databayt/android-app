package org.hogwarts.android.core.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.hogwarts.android.core.database.HogwartsDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Walks real SQLite databases through the authored migrations and checks the
 * result against the exported schema JSON, so a migration that compiles but
 * leaves a column or index behind fails here instead of on a user's phone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HogwartsDatabase::class.java,
    )

    @Test
    fun `oldest shipped version migrates to the current schema`() {
        helper.createDatabase(DB, 7).close()
        helper.runMigrationsAndValidate(DB, CURRENT, true, *ALL_MIGRATIONS).close()
    }

    @Test
    fun `each step with an exported schema validates on its own`() {
        // Schemas 8–18 were never exported; 19 onward are checked one hop at a time.
        for (from in 19 until CURRENT) {
            val name = "step-$from"
            helper.createDatabase(name, from).close()
            helper.runMigrationsAndValidate(name, from + 1, true, *ALL_MIGRATIONS).close()
        }
    }

    @Test
    fun `migrations form an unbroken chain to the current version`() {
        ALL_MIGRATIONS.toList().zipWithNext().forEach { (a, b) ->
            check(a.endVersion == b.startVersion) { "gap between ${a.endVersion} and ${b.startVersion}" }
        }
        check(ALL_MIGRATIONS.last().endVersion == CURRENT)
    }

    private companion object {
        const val DB = "migration-test"
        const val CURRENT = 24
    }
}
