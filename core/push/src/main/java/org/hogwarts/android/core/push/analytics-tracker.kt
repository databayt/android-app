package org.hogwarts.android.core.push

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        })
        Timber.d("Analytics: screen_view - $screenName")
    }

    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        firebaseAnalytics.logEvent(name, Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        })
        Timber.d("Analytics: $name - $params")
    }

    fun logLogin(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    fun logAttendanceMarked(classId: String, count: Int) {
        logEvent("attendance_marked", mapOf("class_id" to classId, "count" to count.toString()))
    }

    fun logGradeViewed(examId: String) {
        logEvent("grade_viewed", mapOf("exam_id" to examId))
    }

    fun logFeePayment(amount: Double, currency: String = "SAR") {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, amount)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
        })
    }
}
