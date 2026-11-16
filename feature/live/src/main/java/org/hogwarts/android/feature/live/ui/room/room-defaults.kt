package org.hogwarts.android.feature.live.ui.room

/**
 * What the room does with the microphone and camera on entry, mirroring the web room
 * (hogwarts `live/room.tsx`): `audio={isHost || (role !== "OBSERVER" && !roomConfig.joinMuted)}`,
 * and the same for video. Teachers come in live; students follow the school's `joinMuted`.
 */
object RoomDefaults {
    fun isHost(role: String?): Boolean = role == "HOST" || role == "CO_HOST"

    /** Whether the microphone (and camera) publish on connect. Observers never publish. */
    fun startsLive(role: String?, joinMuted: Boolean): Boolean =
        isHost(role) || (role != "OBSERVER" && !joinMuted)

    fun micStartsOn(role: String?, joinMuted: Boolean): Boolean = startsLive(role, joinMuted)

    fun cameraStartsOn(role: String?, joinMuted: Boolean): Boolean = startsLive(role, joinMuted)

    /**
     * The persistent "your mic is off" banner: only the teacher, only once the room is up and the
     * entry permission prompt is done, and only while the microphone is not publishing.
     */
    fun showMicBanner(isHost: Boolean, micOn: Boolean, inRoom: Boolean, entrySettled: Boolean): Boolean =
        isHost && inRoom && entrySettled && !micOn
}
