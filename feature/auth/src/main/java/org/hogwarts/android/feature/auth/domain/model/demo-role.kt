package org.hogwarts.android.feature.auth.domain.model

/**
 * The demo school's seeded accounts — mirrors the web's
 * `auth/login/demo-accounts.ts` for the six school-scoped roles. Offered in debug
 * builds only (see [DemoAccounts]).
 */
enum class DemoRole(val email: String) {
    Admin("admin@balqalam.com"),
    Teacher("teacher@balqalam.com"),
    Student("student@balqalam.com"),
    Guardian("parent@balqalam.com"),
    Accountant("accountant@balqalam.com"),
    Staff("staff@balqalam.com"),
}
