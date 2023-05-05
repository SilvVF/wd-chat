package io.silv.feature_chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_chat.ChatViewModel
import io.silv.wifi_direct.P2p

@Module
@InstallIn(ViewModelComponent::class)
object ChatModule {

    @ViewModelScoped
    @Provides
    fun provideChatViewModel(
        p2p: P2p
    ): ChatViewModel = ChatViewModel(p2p)
}