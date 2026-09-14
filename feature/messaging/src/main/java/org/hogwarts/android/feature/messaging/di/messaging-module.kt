package org.hogwarts.android.feature.messaging.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.hogwarts.android.feature.messaging.data.local.ConversationDrafts
import org.hogwarts.android.feature.messaging.data.local.DraftStore
import org.hogwarts.android.feature.messaging.data.remote.MessagingApi
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepository
import org.hogwarts.android.feature.messaging.data.repository.MessagingRepositoryImpl
import org.hogwarts.android.feature.messaging.data.worker.PendingSendScheduler
import org.hogwarts.android.feature.messaging.data.worker.WorkManagerPendingSendScheduler
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MessagingModule {

    @Binds
    @Singleton
    abstract fun bindMessagingRepository(impl: MessagingRepositoryImpl): MessagingRepository

    @Binds
    @Singleton
    abstract fun bindConversationDrafts(impl: DraftStore): ConversationDrafts

    @Binds
    @Singleton
    abstract fun bindPendingSendScheduler(impl: WorkManagerPendingSendScheduler): PendingSendScheduler

    companion object {
        @Provides
        @Singleton
        fun provideMessagingApi(retrofit: Retrofit): MessagingApi = retrofit.create(MessagingApi::class.java)
    }
}
