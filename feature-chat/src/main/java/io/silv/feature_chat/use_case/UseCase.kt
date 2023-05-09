package io.silv.feature_chat.use_case

import arrow.core.Either
import io.silv.WsData
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow


fun interface ConnectToChatUseCase: suspend (Boolean, String) -> Boolean

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>
fun interface SendChatUseCase: suspend (String) -> Unit
fun interface CollectChatUseCase: () -> Either<Throwable, Flow<WsData>>