package io.silv

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.http4k.format.Jackson.auto
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

/**
 *  Listen for messages using given [CoroutineScope] and [Websocket.onMessage].
 *  For each message received a new coroutine is launched.
 *  Calls executable after converting the type using the JSON field type
 */
fun Websocket.suspendOnMessage(
    scope: CoroutineScope,
    mapper: ObjectMapper,
    websocket: Websocket,
    executable: suspend (msg: WsObj, ws: Websocket) -> Unit
) {

    val wsObjLens = WsMessage.auto<WsObj>().toLens()

    onMessage {
        scope.launch {
            val data = runCatching {
                val body = it.bodyString()
                when (wsObjLens(it).type) {
                    ChatMessage.typeName -> mapper.readValue(body, ChatMessage::class.java)
                    else -> return@launch
                }
            }
            executable(data.getOrElse { return@launch }, websocket)
        }
    }
}

internal const val serverPort = 9909