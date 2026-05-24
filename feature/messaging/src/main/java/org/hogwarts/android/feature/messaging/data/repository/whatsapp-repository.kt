package org.hogwarts.android.feature.messaging.data.repository

interface WhatsAppRepository {
    suspend fun getStatus(): WhatsAppStatus
    suspend fun connect(): WhatsAppQRData
    suspend fun disconnect()
    suspend fun toggleConversationWhatsApp(conversationId: String, enabled: Boolean)
}

data class WhatsAppStatus(
    val status: String,
    val phone: String? = null,
    val instanceName: String? = null,
) {
    val isConnected: Boolean get() = status == "connected"
}

data class WhatsAppQRData(
    val qrCode: String?,
    val status: String,
)
