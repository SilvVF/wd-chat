package io.silv.feature_chat.use_case

import android.net.Uri
import arrow.core.Either
import io.silv.feature_chat.types.MyChat
import io.silv.feature_chat.types.UiWsData
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.Flow


fun interface ConnectToChatUseCase: suspend (Boolean, String) -> Boolean

fun interface ObserveWifiDirectEventsUseCase: () -> Flow<WifiP2pEvent>

fun interface SendChatUseCase: suspend (String, List<Uri>) -> MyChat

fun interface CollectChatUseCase: () -> Either<Throwable, Flow<UiWsData>>

fun interface WriteToAttachmentsUseCase: suspend (Uri) -> Uri

fun interface DeleteAttachmentUseCase: suspend (Uri) -> Unit

fun interface ShutdownServerUseCase: suspend() -> Unit