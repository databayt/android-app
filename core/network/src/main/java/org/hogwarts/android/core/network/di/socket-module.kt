package org.hogwarts.android.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.network.BuildConfig
import org.hogwarts.android.core.network.socket.SocketManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketManager(json: Json): SocketManager =
        SocketManager(socketUrl = BuildConfig.SOCKET_URL, json = json)
}
