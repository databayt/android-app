package org.hogwarts.android.core.push

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    fun initialize(userId: String? = null) {
        userId?.let { crashlytics.setUserId(it) }
        Timber.d("CrashReporter initialized")
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setSchoolId(schoolId: String) {
        crashlytics.setCustomKey("school_id", schoolId)
    }

    fun setUserRole(role: String) {
        crashlytics.setCustomKey("user_role", role)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
        Timber.e(throwable, "Non-fatal exception recorded")
    }

    fun recordException(throwable: Throwable, context: Map<String, String>) {
        context.forEach { (key, value) -> crashlytics.setCustomKey(key, value) }
        crashlytics.recordException(throwable)
        Timber.e(throwable, "Non-fatal exception recorded with context: $context")
    }
}
