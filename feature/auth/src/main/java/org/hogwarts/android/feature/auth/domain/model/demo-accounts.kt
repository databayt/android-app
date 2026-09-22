package org.hogwarts.android.feature.auth.domain.model

/**
 * The demo school's accounts, behind "Try demo" on the login page. They are
 * public on the web — demo.balqalam.com offers the same role picker — so the
 * phone offers them in every build, not only in debug.
 */
object DemoAccounts {
    val roles: List<DemoRole> = DemoRole.entries

    /** = DEMO_PASSWORD in hogwarts `prisma/seeds/constants.ts`. */
    const val password: String = "1234"
}
