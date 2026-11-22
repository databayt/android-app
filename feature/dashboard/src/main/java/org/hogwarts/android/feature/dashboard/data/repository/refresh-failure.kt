package org.hogwarts.android.feature.dashboard.data.repository

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Why a dashboard refresh did not produce fresh data. */
enum class RefreshFailure {
    /** The device could not reach the network at all (DNS, refused connection, no route). */
    Connectivity,

    /** The request was sent but the answer did not come in time — a slow server, not an offline phone. */
    Timeout,

    /** The server answered with an error, or the answer was unusable (expired sign-in included). */
    Server,
}

/**
 * Only a failure that proves the device cannot connect is [RefreshFailure.Connectivity]. A timeout
 * is [RefreshFailure.Timeout]; every other error (reset, TLS, bad body) is [RefreshFailure.Server]:
 * it must not be presented as "you are offline".
 */
fun classifyFailure(error: Throwable): RefreshFailure = when (error) {
    is SocketTimeoutException -> RefreshFailure.Timeout
    is UnknownHostException, is ConnectException, is NoRouteToHostException -> RefreshFailure.Connectivity
    else -> RefreshFailure.Server
}
