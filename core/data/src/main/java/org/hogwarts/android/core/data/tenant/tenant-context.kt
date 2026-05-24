package org.hogwarts.android.core.data.tenant

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exception thrown when no tenant context is available
 */
class NoTenantContextException : Exception("No tenant context available. User must be authenticated.")

/**
 * Exception thrown when user is not authorized
 */
class UnauthorizedException(message: String = "Unauthorized") : Exception(message)

/**
 * Provides the current tenant (school) context for multi-tenant isolation.
 *
 * CRITICAL: Every API call and Room query MUST be scoped by schoolId.
 * Use this class to get the current schoolId from the authenticated user's JWT.
 *
 * Usage:
 * ```kotlin
 * class StudentRepositoryImpl @Inject constructor(
 *     private val tenantContext: TenantContext
 * ) : StudentRepository {
 *     override suspend fun getStudents(): List<Student> {
 *         val schoolId = tenantContext.requireSchoolId()
 *         return api.getStudents(schoolId = schoolId)
 *     }
 * }
 * ```
 */
@Singleton
class TenantContext @Inject constructor(
    private val sessionManager: SessionManager
) {
    /**
     * Get current school ID or null if not authenticated
     */
    val schoolId: String?
        get() = sessionManager.currentUser?.schoolId

    /**
     * Get current user ID or null if not authenticated
     */
    val userId: String?
        get() = sessionManager.currentUser?.id

    /**
     * Get current user display name or null if not authenticated
     */
    val userName: String?
        get() = sessionManager.currentUser?.displayName

    /**
     * Get current user role or null if not authenticated
     */
    val userRole: UserRole?
        get() = sessionManager.currentUser?.role

    /**
     * Grade level (1..12) for STUDENT-role users; null for other roles or when
     * the backend hasn't supplied it yet. Used by the Stream catalog to skip
     * the manual grade picker for students.
     */
    val studentGrade: Int?
        get() = sessionManager.currentUser?.grade

    /**
     * Require school ID or throw exception
     * Use this in repositories to ensure tenant isolation
     */
    fun requireSchoolId(): String =
        schoolId ?: throw NoTenantContextException()

    /**
     * Require user ID or throw exception
     */
    fun requireUserId(): String =
        userId ?: throw UnauthorizedException("User not authenticated")

    /**
     * Check if user is authenticated
     */
    val isAuthenticated: Boolean
        get() = sessionManager.isAuthenticated

    /**
     * Check if current user has a specific role
     */
    fun hasRole(role: UserRole): Boolean =
        userRole == role

    /**
     * Check if current user has any of the specified roles
     */
    fun hasAnyRole(vararg roles: UserRole): Boolean =
        userRole in roles
}

/**
 * User roles matching Hogwarts backend
 */
enum class UserRole {
    STUDENT,
    TEACHER,
    GUARDIAN,
    ADMIN,
    SUPER_ADMIN
}

/**
 * Current authenticated user info
 */
data class CurrentUser(
    val id: String,
    val email: String,
    val schoolId: String,
    val role: UserRole,
    val givenName: String?,
    val familyName: String?,
    val grade: Int? = null
) {
    val displayName: String
        get() = listOfNotNull(givenName, familyName).joinToString(" ").ifEmpty { email }
}

/**
 * Session manager interface - implemented in security module
 */
interface SessionManager {
    val currentUser: CurrentUser?
    val isAuthenticated: Boolean
    suspend fun setUser(user: CurrentUser)
    suspend fun clearSession()
}
