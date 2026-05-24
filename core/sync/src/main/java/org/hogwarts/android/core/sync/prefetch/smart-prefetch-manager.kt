package org.hogwarts.android.core.sync.prefetch

import android.content.Context
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartPrefetchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    fun shouldPrefetch(): Boolean {
        val batteryLevel = getBatteryLevel()
        if (batteryLevel < 20) {
            Timber.d("Battery too low ($batteryLevel%), skipping prefetch")
            return false
        }
        return true
    }

    fun getPrefetchPriority(): List<PrefetchTask> {
        val tasks = mutableListOf<PrefetchTask>()

        // Always prefetch today's schedule
        tasks.add(PrefetchTask("timetable", PrefetchPriority.HIGH, "Today's schedule"))

        // Prefetch upcoming exams within 1 week
        tasks.add(PrefetchTask("exams", PrefetchPriority.HIGH, "Upcoming exams"))

        // Prefetch unread messages
        tasks.add(PrefetchTask("messages", PrefetchPriority.MEDIUM, "Unread messages"))

        // Prefetch recent grades
        tasks.add(PrefetchTask("grades", PrefetchPriority.MEDIUM, "Recent grades"))

        // Low priority: library, events
        tasks.add(PrefetchTask("library", PrefetchPriority.LOW, "Library catalog"))
        tasks.add(PrefetchTask("events", PrefetchPriority.LOW, "Upcoming events"))

        // Filter by battery level
        val batteryLevel = getBatteryLevel()
        return when {
            batteryLevel < 30 -> tasks.filter { it.priority == PrefetchPriority.HIGH }
            batteryLevel < 60 -> tasks.filter { it.priority != PrefetchPriority.LOW }
            else -> tasks
        }
    }

    private fun getBatteryLevel(): Int {
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    }
}

data class PrefetchTask(
    val entityType: String,
    val priority: PrefetchPriority,
    val description: String
)

enum class PrefetchPriority {
    HIGH, MEDIUM, LOW
}
