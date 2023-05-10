package io.silv.feature_chat.use_case

import android.net.Uri
import arrow.core.Either
import io.silv.ChatMessage
import io.silv.Image

import io.silv.WsData
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.image_store.ImageRepository
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext

internal suspend fun sendChatUseCaseImpl(
    websocketRepo: WebsocketRepo,
    imageRepository: ImageRepository,
    message: String,
    uris: List<Uri>
) {
    val images = withContext(Dispatchers.IO) {
        uris.map {
            async { imageRepository.getFileFromUri(it) }
        }
            .awaitAll()
            .mapNotNull { file -> file?.readBytes() }
            .map { bytes ->
                Image(data = bytes,)
            }
    }
    runCatching {
        websocketRepo.send(
            ChatMessage(
                message = message,
                name = "",
                address = "0.0.0.0",
                images = images
            )
        )
    }.onFailure { it.printStackTrace() }
}
internal suspend fun connectToChatUseCaseImpl(
    isGroupOwner: Boolean,
    groupOwnerAddress: String,
    websocketRepo: WebsocketRepo
): Boolean {
    runCatching {
        websocketRepo.startConnection(
            groupOwner = isGroupOwner,
            groupOwnerAddress = groupOwnerAddress
        )
    }
        .onFailure {
            it.printStackTrace()
        }
    return true
}

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): SharedFlow<WifiP2pEvent> {
    return wifiP2pReceiver.eventBroadcast
}

internal fun collectChatUseCaseImpl(
    websocketRepo: WebsocketRepo
): Either<Throwable, Flow<WsData>> {
    return Either.catch {
        websocketRepo.getReceiveFlow()
    }
}

