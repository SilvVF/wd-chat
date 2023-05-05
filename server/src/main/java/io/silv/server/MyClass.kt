package io.silv.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.websockets
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

val chatPath = Path.of("chat")

val lens = Body.auto<WsObj>().toLens()
class ChatWebSocketHandler(
    var groupOwner: Boolean,
    var groupOwnerAddress: String,
    scope: CoroutineScope
) {

    private val mutWsFlow = MutableSharedFlow<WsObj>()
    val wsFlow = mutWsFlow.asSharedFlow()

    fun start() {
        ws.use {
            start()
        }
    }

    val ws = websockets {
        "/{chat}" bind { ws: Websocket ->
            val chat = chatPath(ws.upgradeRequest)

            ws.send(WsMessage("Connected"))

            ws.suspendOnMessage(scope) { msg ->
                when(msg) {
                    is ChatMessage -> mutWsFlow.emit(msg)
                }
            }
        }
    }.asServer(Undertow(8978))


}

fun Websocket.suspendOnMessage(
    scope: CoroutineScope,
    executable: suspend (msg: WsObj) -> Unit
) {

    val mapper = jacksonObjectMapper()
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
            executable(data.getOrElse { return@launch })
        }
    }
}



