package org.hogwarts.android.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.core.data.tenant.CurrentUser
import org.hogwarts.android.core.data.tenant.SessionManager
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.common.api.TokenProvider
import org.hogwarts.android.core.network.interceptor.TenantProvider
import org.hogwarts.android.core.security.TokenManager
import org.hogwarts.android.feature.auth.data.repository.AuthRepository
import org.hogwarts.android.feature.auth.data.repository.AuthRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenProvider(tokenManager: TokenManager): TokenProvider

    @Binds
    @Singleton
    abstract fun bindTenantProvider(tenantContext: TenantProviderImpl): TenantProvider

    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager
}

/**
 * Implementation of TenantProvider using TenantContext.
 */
class TenantProviderImpl @javax.inject.Inject constructor(
    private val sessionManager: SessionManager
) : TenantProvider {
    override val schoolId: String?
        get() = sessionManager.currentUser?.schoolId
}

/**
 * Implementation of SessionManager.
 *
 * Persists CurrentUser to EncryptedSharedPreferences so the tenant context
 * (schoolId, role, userId) survives process death. Without this, a cold start
 * with a valid JWT would have no schoolId, causing NoTenantContextException.
 */
class SessionManagerImpl @javax.inject.Inject constructor(
    @ApplicationContext private val context: Context
) : SessionManager {

    private companion object {
        const val PREFS_FILE = "hogwarts_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        const val KEY_SCHOOL_ID = "school_id"
        const val KEY_ROLE = "role"
        const val KEY_GIVEN_NAME = "given_name"
        const val KEY_FAMILY_NAME = "family_name"
        const val KEY_GRADE = "grade"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Volatile
    private var _currentUser: CurrentUser? = restoreUser()

    override val currentUser: CurrentUser?
        get() = _currentUser

    override val isAuthenticated: Boolean
        get() = _currentUser != null

    override suspend fun setUser(user: CurrentUser) {
        _currentUser = user
        val editor = prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_SCHOOL_ID, user.schoolId)
            .putString(KEY_ROLE, user.role.name)
            .putString(KEY_GIVEN_NAME, user.givenName)
            .putString(KEY_FAMILY_NAME, user.familyName)
        if (user.grade != null) {
            editor.putString(KEY_GRADE, user.grade.toString())
        } else {
            editor.remove(KEY_GRADE)
        }
        editor.apply()
    }

    override suspend fun clearSession() {
        _currentUser = null
        prefs.edit().clear().apply()
    }

    private fun restoreUser(): CurrentUser? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val schoolId = prefs.getString(KEY_SCHOOL_ID, null) ?: return null
        val roleName = prefs.getString(KEY_ROLE, null) ?: return null
        val role = try { UserRole.valueOf(roleName) } catch (_: Exception) { return null }
        return CurrentUser(
            id = id,
            email = email,
            schoolId = schoolId,
            role = role,
            givenName = prefs.getString(KEY_GIVEN_NAME, null),
            familyName = prefs.getString(KEY_FAMILY_NAME, null),
            grade = prefs.getString(KEY_GRADE, null)?.toIntOrNull()
        )
    }
}
