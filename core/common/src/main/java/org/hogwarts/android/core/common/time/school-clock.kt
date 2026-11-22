package org.hogwarts.android.core.common.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The school's time zone: the zone the server sent with the school if any, else the brand's
 * (the school build's `timezone`), else the device's last. Class periods are wall-clock times in
 * the school's zone, so a teacher in Bahrain (UTC+3) with a Khartoum school (UTC+2) must not see
 * every class as missed the moment it starts just because the phone is an hour ahead.
 */
class SchoolZone(brandZone: String, private val now: () -> Instant = Instant::now) {
    private val brand: ZoneId? = parse(brandZone)
    @Volatile private var server: ZoneId? = null

    val id: ZoneId get() = server ?: brand ?: ZoneId.systemDefault()

    /** A zone the server sent with the school; blank or unknown values are ignored. */
    fun accept(serverZone: String?) { parse(serverZone)?.let { server = it } }

    /** 0 = Sunday … 6 = Saturday, in the school's zone. */
    fun weekday(): Int = now().atZone(id).dayOfWeek.value % 7

    /** A clock that tells the school's time; `id` is read on every call, so a later [accept] takes effect. */
    fun clock(): Clock = object : Clock() {
        override fun getZone(): ZoneId = id
        override fun withZone(zone: ZoneId): Clock = Clock.system(zone)
        override fun instant(): Instant = now()
    }

    private fun parse(value: String?): ZoneId? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { ZoneId.of(it) }.getOrNull() }
}
