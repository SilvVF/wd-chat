package io.silv.feature_chat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_chat.ChatViewModel
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.GetGroupInfoUseCase
import io.silv.feature_chat.use_case.collectChatUseCaseImpl
import io.silv.feature_chat.use_case.connectToChatUseCaseImpl
import io.silv.feature_chat.use_case.getGroupInfoUseCaseImpl
import io.silv.wifi_direct.P2p
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(ViewModelComponent::class)
object ChatModule {

    @ViewModelScoped
    @Provides
    fun provideChatViewModel(
        getGroupInfoUseCase: GetGroupInfoUseCase,
        connectToChatUseCase: ConnectToChatUseCase,
        collectChatUseCase: CollectChatUseCase
    ): ChatViewModel = ChatViewModel(
        getGroupInfoUseCase = getGroupInfoUseCase,
        connectToChatUseCase = connectToChatUseCase,
        collectChatUseCase = collectChatUseCase
    )

    @ViewModelScoped
    @Provides
    fun provideWebSocketRepo(): WebsocketRepo {
        return WebsocketRepo(CoroutineScope(Dispatchers.IO))
    }

    @ViewModelScoped
    @Provides
    fun provideCollectChatUseCase(
        websocketRepo: WebsocketRepo
    ) = CollectChatUseCase {
        collectChatUseCaseImpl(websocketRepo)
    }

    @ViewModelScoped
    @Provides
    fun provideGetGroupInfoUseCase(
        p2p: P2p
    ): GetGroupInfoUseCase = GetGroupInfoUseCase {
        getGroupInfoUseCaseImpl(p2p)
    }

    @ViewModelScoped
    @Provides
    fun provideConnectToChatUseCase(
        websocketRepo: WebsocketRepo
    ): ConnectToChatUseCase = ConnectToChatUseCase { owner, address ->
        connectToChatUseCaseImpl(
            isGroupOwner = owner,
            groupOwnerAddress = address,
            websocketRepo = websocketRepo
        )
    }
}