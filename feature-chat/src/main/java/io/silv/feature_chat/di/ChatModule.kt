package io.silv.feature_chat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_chat.ChatViewModel
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_chat.use_case.SendChatUseCase
import io.silv.feature_chat.use_case.collectChatUseCaseImpl
import io.silv.feature_chat.use_case.connectToChatUseCaseImpl
import io.silv.feature_chat.use_case.observeWifiDirectEventsUseCaseImpl
import io.silv.feature_chat.use_case.sendChatUseCaseImpl
import io.silv.image_store.ImageRepository
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(ViewModelComponent::class)
object ChatModule {

    @ViewModelScoped
    @Provides
    fun provideChatViewModel(
        observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
        connectToChatUseCase: ConnectToChatUseCase,
        collectChatUseCase: CollectChatUseCase,
        sendChatUseCase: SendChatUseCase,
        imageRepository: ImageRepository
    ): ChatViewModel = ChatViewModel(
        observeWifiDirectEventsUseCase = observeWifiDirectEventsUseCase,
        connectToChatUseCase = connectToChatUseCase,
        collectChatUseCase = collectChatUseCase,
        sendChatUseCase = sendChatUseCase,
        imageStore = imageRepository
    )

    @ViewModelScoped
    @Provides
    fun provideObserveWifiDirectEventsUseCase(
        receiver: WifiP2pReceiver
    ) = ObserveWifiDirectEventsUseCase {
        observeWifiDirectEventsUseCaseImpl(receiver)
    }

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
    fun provideImageStore(
        @ApplicationContext context: Context
    ): ImageRepository  {
        return ImageRepository.getImpl(context)
    }

    @ViewModelScoped
    @Provides
    fun provideSendChatUseCase(
        websocketRepo: WebsocketRepo
    ) = SendChatUseCase {
        sendChatUseCaseImpl(websocketRepo, it)
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