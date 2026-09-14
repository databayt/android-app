package org.hogwarts.android.feature.auth.domain.model

import org.hogwarts.android.feature.auth.BuildConfig

/**
 * The demo school's role picker is a debug-build tool: release builds get an
 * empty roster and never show it. The accounts themselves are public on the
 * web (demo.balqalam.com offers the same picker).
 */
object DemoAccounts {
    val roles: List<DemoRole> = if (BuildConfig.DEBUG) DemoRole.entries else emptyList()

    /** = DEMO_PASSWORD in hogwarts `prisma/seeds/constants.ts`. */
    val password: String get() = if (BuildConfig.DEBUG) "1234" else ""
}
