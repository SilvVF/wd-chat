package io.silv.feature_chat.di

import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.datastore.EncryptedDatastore
import io.silv.feature_chat.ChatViewModel
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.DeleteAttachmentUseCase
import io.silv.feature_chat.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_chat.use_case.SendChatUseCase
import io.silv.feature_chat.use_case.WriteToAttachmentsUseCase
import io.silv.feature_chat.use_case.collectChatUseCaseImpl
import io.silv.feature_chat.use_case.connectToChatUseCaseImpl
import io.silv.feature_chat.use_case.deleteAttachmentUseCaseImpl
import io.silv.feature_chat.use_case.observeWifiDirectEventsUseCaseImpl
import io.silv.feature_chat.use_case.sendChatUseCaseImpl
import io.silv.feature_chat.use_case.writeToAttachmentsUseCaseImpl
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
        savedStateHandle: SavedStateHandle,
        observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
        connectToChatUseCase: ConnectToChatUseCase,
        collectChatUseCase: CollectChatUseCase,
        sendChatUseCase: SendChatUseCase,
        writeToAttachmentsUseCase: WriteToAttachmentsUseCase,
        deleteAttachmentUseCase: DeleteAttachmentUseCase,
        datastore: EncryptedDatastore
    ): ChatViewModel = ChatViewModel(
        observeWifiDirectEventsUseCase = observeWifiDirectEventsUseCase,
        connectToChatUseCase = connectToChatUseCase,
        collectChatUseCase = collectChatUseCase,
        sendChatUseCase = sendChatUseCase,
        writeToAttachmentsUseCase = writeToAttachmentsUseCase,
        deleteAttachmentUseCase = deleteAttachmentUseCase,
        datastore = datastore,
        savedStateHandle = savedStateHandle
    )

    @ViewModelScoped
    @Provides
    fun provideDeleteAttachmentUseCase(
        imageRepository: ImageRepository,
    ) = DeleteAttachmentUseCase {
        deleteAttachmentUseCaseImpl(it, imageRepository)
    }

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
    fun provideWriteToAttachmentsUseCase(
        imageRepository: ImageRepository
    ) = WriteToAttachmentsUseCase {
        writeToAttachmentsUseCaseImpl(imageRepository, it)
    }

    @ViewModelScoped
    @Provides
    fun provideCollectChatUseCase(
        websocketRepo: WebsocketRepo,
        imageRepository: ImageRepository
    ) = CollectChatUseCase {
        collectChatUseCaseImpl(websocketRepo, imageRepository)
    }

    @ViewModelScoped
    @Provides
    fun provideSendChatUseCase(
        websocketRepo: WebsocketRepo,
        imageRepository: ImageRepository
    ) = SendChatUseCase { message, uris ->
        sendChatUseCaseImpl(websocketRepo, imageRepository, message, uris)
    }

    @ViewModelScoped
    @Provides
    fun provideConnectToChatUseCase(
        websocketRepo: WebsocketRepo,
        datastore: EncryptedDatastore,
        imageRepository: ImageRepository
    ): ConnectToChatUseCase = ConnectToChatUseCase { owner, address ->
        connectToChatUseCaseImpl(
            isGroupOwner = owner,
            groupOwnerAddress = address,
            websocketRepo = websocketRepo,
            datastore, imageRepository
        )
    }
}