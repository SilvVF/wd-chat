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

fun sendChatUseCaseImpl(
    message: String
) {

}

suspend fun connectToChatUseCaseImpl(
    wifiP2pGroup: WifiP2pGroup,
    websocketRepo: WebsocketRepo
): Boolean {
    return runCatching {
        websocketRepo.startConnection(
            groupOwner = wifiP2pGroup.isGroupOwner,
            groupOwnerAddress = wifiP2pGroup.owner.deviceAddress
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
): Either<P2pError, Flow<WifiP2pGroup>> {
    return either {
        // get initial group info without waiting on changes
        val info = p2p.requestGroupInfo().bind()
        flow {
            //emit initial info
            emit(info)
            // emit any changes to group info
            p2p.groupInfoFlow.collect {
                emit(it)
            }
        }
    }
}
