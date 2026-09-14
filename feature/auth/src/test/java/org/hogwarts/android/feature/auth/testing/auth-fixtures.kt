package org.hogwarts.android.feature.auth.testing

import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.auth.domain.model.AuthResult

val fakeAuthResult = AuthResult(
    userId = "user-1",
    email = "admin@balqalam.com",
    schoolId = "school-1",
    role = UserRole.ADMIN,
    givenName = "Albus",
    familyName = "Dumbledore",
    accessToken = "access",
    refreshToken = "refresh",
    expiresAt = 1_789_484_272_103,
)
