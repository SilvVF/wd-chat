package io.silv.feature_chat.use_case

import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.ChatMessage

import io.silv.WsData
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

private var go = false
suspend fun sendChatUseCaseImpl(
    websocketRepo: WebsocketRepo,
    message: String
) {
    runCatching {
        websocketRepo.send(
            ChatMessage(
                message = message,
                name = "groupOwner - $go",
                address = "0.0.0.0",
            )
        )
    }.onFailure { it.printStackTrace() }
}
suspend fun connectToChatUseCaseImpl(
    isGroupOwner: Boolean,
    groupOwnerAddress: String,
    websocketRepo: WebsocketRepo
): Boolean {
    go = isGroupOwner
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

fun collectChatUseCaseImpl(
    websocketRepo: WebsocketRepo
): Either<Throwable, Flow<WsData>> {
    return Either.catch {
        websocketRepo.getReceiveFlow()
    }
}


suspend fun getGroupInfoUseCaseImpl(
    p2p: P2p
): Either<P2pError, WifiP2pGroup> {
    return p2p.requestGroupInfo()
}
