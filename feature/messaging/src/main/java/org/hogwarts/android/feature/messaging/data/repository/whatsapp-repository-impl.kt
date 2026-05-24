package org.hogwarts.android.feature.messaging.data.repository

import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.remote.WhatsAppToggleBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppRepositoryImpl @Inject constructor(
    private val api: MessagingApi,
) : WhatsAppRepository {

    override suspend fun getStatus(): WhatsAppStatus {
        val response = api.getWhatsAppStatus()
        val body = response.body() ?: throw Exception("Failed to get WhatsApp status")
        return WhatsAppStatus(
            status = body.status,
            phone = body.phone,
            instanceName = body.instanceName,
        )
    }

    override suspend fun connect(): WhatsAppQRData {
        val response = api.connectWhatsApp()
        val body = response.body() ?: throw Exception("Failed to connect WhatsApp")
        return WhatsAppQRData(qrCode = body.qrCode, status = body.status)
    }

    override suspend fun disconnect() {
        api.disconnectWhatsApp()
    }

    override suspend fun toggleConversationWhatsApp(conversationId: String, enabled: Boolean) {
        api.toggleConversationWhatsApp(conversationId, WhatsAppToggleBody(enabled))
    }
}
