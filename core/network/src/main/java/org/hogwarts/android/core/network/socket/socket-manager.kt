package org.hogwarts.android.core.network.socket

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class SocketConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED,
}

/**
 * Central Socket.IO client singleton.
 *
 * Manages connection lifecycle, room subscriptions, and event routing.
 * Matches the web app's socket-service.ts event contract.
 */
@Singleton
class SocketManager @Inject constructor(
    private val socketUrl: String,
    private val json: Json,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null
    private val listeners = mutableMapOf<String, MutableList<(JSONObject) -> Unit>>()
    private val joinedRooms = mutableSetOf<String>()

    private val _connectionState = MutableStateFlow(SocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SocketConnectionState> = _connectionState.asStateFlow()

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var heartbeatJob: kotlinx.coroutines.Job? = null

    fun connect(accessToken: String, schoolId: String, userId: String) {
        if (socket?.connected() == true) return

        _connectionState.value = SocketConnectionState.CONNECTING
        reconnectAttempts = 0

        try {
            val opts = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
                forceNew = true
                reconnection = false // We handle reconnection ourselves
                query = "token=$accessToken&schoolId=$schoolId&userId=$userId"
            }

            socket = IO.socket(socketUrl, opts).apply {
                on(Socket.EVENT_CONNECT) {
                    Timber.d("Socket.IO connected")
                    _connectionState.value = SocketConnectionState.CONNECTED
                    reconnectAttempts = 0
                    rejoinRooms()
                    startHeartbeat()
                }

                on(Socket.EVENT_DISCONNECT) {
                    Timber.d("Socket.IO disconnected")
                    _connectionState.value = SocketConnectionState.DISCONNECTED
                    stopHeartbeat()
                    attemptReconnect(accessToken, schoolId, userId)
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    val error = args.firstOrNull()
                    Timber.e("Socket.IO connect error: $error")
                    _connectionState.value = SocketConnectionState.FAILED
                    attemptReconnect(accessToken, schoolId, userId)
                }

                // Route all custom events to registered listeners
                SOCKET_EVENTS.forEach { event ->
                    on(event) { args ->
                        val data = args.firstOrNull() as? JSONObject ?: return@on
                        listeners[event]?.forEach { callback ->
                            try {
                                callback(data)
                            } catch (e: Exception) {
                                Timber.e(e, "Error in socket listener for $event")
                            }
                        }
                    }
                }

                connect()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create socket")
            _connectionState.value = SocketConnectionState.FAILED
        }
    }

    fun disconnect() {
        stopHeartbeat()
        joinedRooms.clear()
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = SocketConnectionState.DISCONNECTED
    }

    fun on(event: String, callback: (JSONObject) -> Unit): () -> Unit {
        listeners.getOrPut(event) { mutableListOf() }.add(callback)
        return { listeners[event]?.remove(callback) }
    }

    fun emit(event: String, data: Map<String, Any?>) {
        try {
            socket?.emit(event, JSONObject(data))
        } catch (e: Exception) {
            Timber.e(e, "Failed to emit $event")
        }
    }

    fun joinRoom(room: String) {
        joinedRooms.add(room)
        socket?.emit("join", room)
    }

    fun leaveRoom(room: String) {
        joinedRooms.remove(room)
        socket?.emit("leave", room)
    }

    fun subscribeToConversation(conversationId: String) =
        joinRoom("conversation:$conversationId")

    fun unsubscribeFromConversation(conversationId: String) =
        leaveRoom("conversation:$conversationId")

    private fun rejoinRooms() {
        joinedRooms.forEach { room ->
            socket?.emit("join", room)
        }
    }

    private fun attemptReconnect(token: String, schoolId: String, userId: String) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            _connectionState.value = SocketConnectionState.FAILED
            return
        }
        _connectionState.value = SocketConnectionState.RECONNECTING
        reconnectAttempts++
        val delayMs = (1000L * (1 shl (reconnectAttempts - 1))).coerceAtMost(10_000L)
        scope.launch {
            delay(delayMs)
            Timber.d("Socket.IO reconnect attempt $reconnectAttempts/$maxReconnectAttempts")
            socket?.off()
            socket = null
            connect(token, schoolId, userId)
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                delay(60_000L)
                socket?.emit("presence:heartbeat", JSONObject())
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    companion object {
        val SOCKET_EVENTS = listOf(
            "message:new",
            "message:updated",
            "message:deleted",
            "message:read",
            "message:reaction",
            "message:delivered",
            "conversation:new",
            "conversation:updated",
            "conversation:archived",
            "conversation:participant_added",
            "conversation:participant_removed",
            "typing:start",
            "typing:stop",
            "presence:online",
            "presence:offline",
            "presence:list",
        )
    }
}
