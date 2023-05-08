package io.silv.feature_chat.use_case

import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import arrow.core.raise.either
import io.silv.WsObj
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


suspend fun connectToChatUseCaseImpl(
    isGroupOwner: Boolean,
    groupOwnerAddress: String,
    websocketRepo: WebsocketRepo
): Boolean {
    return runCatching {
        websocketRepo.startConnection(
            groupOwner = isGroupOwner,
            groupOwnerAddress = groupOwnerAddress
        )
        true
    }
        .onFailure {
            it.printStackTrace()
        }
        .getOrDefault(false)
}

fun collectChatUseCaseImpl(
    websocketRepo: WebsocketRepo
): Either<Throwable, Flow<WsObj>> {
    return Either.catch {
        websocketRepo.getReceiveFlow()
    }
}


suspend fun getGroupInfoUseCaseImpl(
    p2p: P2p
): Either<P2pError, WifiP2pGroup> {
    return p2p.requestGroupInfo()
}
