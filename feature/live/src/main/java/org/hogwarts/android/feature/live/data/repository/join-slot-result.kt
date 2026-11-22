package org.hogwarts.android.feature.live.data.repository

import org.hogwarts.android.feature.live.data.remote.dto.JoinSlotDto

/** What asking the server to resolve a timetable period came back with. */
sealed interface JoinSlotResult {
    /** The server answered; read [JoinSlotDto.kind]. */
    data class Resolved(val slot: JoinSlotDto) : JoinSlotResult
    /** HTTP 404 / 405 or a body that is not the endpoint's JSON: the server does not have it yet. */
    data object NotDeployed : JoinSlotResult
    /** HTTP 403. */
    data object Forbidden : JoinSlotResult
    /** No answer (offline, a 5xx): worth asking again. */
    data class Failed(val message: String?) : JoinSlotResult
}
