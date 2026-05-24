package org.hogwarts.android.feature.messaging.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.data.tenant.TenantContext
import org.hogwarts.android.core.database.dao.ConversationDao
import org.hogwarts.android.core.database.dao.MessageDao
import org.hogwarts.android.core.database.dao.PendingMessageDao
import org.hogwarts.android.core.network.socket.SocketManager
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepositoryImpl
import org.hogwarts.android.feature.messaging.data.repository.WhatsAppRepository
import org.hogwarts.android.feature.messaging.data.repository.WhatsAppRepositoryImpl
import org.hogwarts.android.feature.messaging.data.socket.MessagingSocketHandler
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {

    @Provides
    @Singleton
    fun provideMessagingApi(retrofit: Retrofit): MessagingApi =
        retrofit.create(MessagingApi::class.java)

    @Provides
    @Singleton
    fun provideMessagingSocketHandler(
        socketManager: SocketManager,
        messageDao: MessageDao,
        conversationDao: ConversationDao,
        tenantContext: TenantContext,
        json: Json,
    ): MessagingSocketHandler = MessagingSocketHandler(
        socketManager, messageDao, conversationDao, tenantContext, json,
    )

    @Provides
    @Singleton
    fun provideWhatsAppRepository(
        api: MessagingApi,
    ): WhatsAppRepository = WhatsAppRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideMessagingRepository(
        api: MessagingApi,
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        pendingMessageDao: PendingMessageDao,
        tenantContext: TenantContext,
        socketManager: SocketManager,
        socketHandler: MessagingSocketHandler,
    ): MessagingRepository {
        val repo = MessagingRepositoryImpl(
            api, conversationDao, messageDao, pendingMessageDao, tenantContext, socketManager,
        )
        socketHandler.repository = repo
        return repo
    }
}
