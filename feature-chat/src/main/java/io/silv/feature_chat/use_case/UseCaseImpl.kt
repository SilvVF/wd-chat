package io.silv.feature_chat.use_case

import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.ChatMessage

import io.silv.WsData
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

internal suspend fun sendChatUseCaseImpl(
    websocketRepo: WebsocketRepo,
    message: String
) {
    runCatching {
        websocketRepo.send(
            ChatMessage(
                message = message,
                name = "",
                address = "0.0.0.0",
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

