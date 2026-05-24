package org.hogwarts.android.core.common.performance

import android.os.SystemClock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceTracker @Inject constructor() {

    private val activeTraces = mutableMapOf<String, Long>()

    fun startTrace(name: String) {
        activeTraces[name] = SystemClock.elapsedRealtime()
        Timber.d("Performance: Started trace '$name'")
    }

    fun endTrace(name: String): Long {
        val startTime = activeTraces.remove(name) ?: run {
            Timber.w("Performance: No active trace found for '$name'")
            return -1
        }
        val duration = SystemClock.elapsedRealtime() - startTime
        Timber.d("Performance: Trace '$name' completed in ${duration}ms")
        return duration
    }

    fun measureBlock(name: String, block: () -> Unit): Long {
        startTrace(name)
        block()
        return endTrace(name)
    }

    suspend fun measureSuspend(name: String, block: suspend () -> Unit): Long {
        startTrace(name)
        block()
        return endTrace(name)
    }
}
