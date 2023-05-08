package io.silv.feature_chat.use_case

import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.WsData
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

fun interface GetGroupInfoUseCase: suspend () -> Either<P2pError, WifiP2pGroup>

fun interface ConnectToChatUseCase: suspend (Boolean, String) -> Boolean

fun interface SendChatUseCase: suspend (String) -> Unit
fun interface CollectChatUseCase: () -> Either<Throwable, Flow<WsData>>