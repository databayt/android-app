package org.hogwarts.android.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import org.hogwarts.android.core.network.socket.SocketManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    private const val SOCKET_URL = "https://ed.databayt.org"

    @Provides
    @Singleton
    fun provideSocketManager(json: Json): SocketManager =
        SocketManager(socketUrl = SOCKET_URL, json = json)
}
