package io.silv.feature_chat.use_case

import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.WsObj
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

fun interface GetGroupInfoUseCase: suspend () -> Either<P2pError, Flow<WifiP2pGroup>>

fun interface ConnectToChatUseCase: suspend (WifiP2pGroup) -> Boolean

fun interface CollectChatUseCase: () -> Either<Throwable, Flow<WsObj>>