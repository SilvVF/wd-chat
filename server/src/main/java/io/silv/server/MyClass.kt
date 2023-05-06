package io.silv.server

import com.fasterxml.jackson.databind.ObjectMapper
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


const val serverPort = 9909

val lens = Body.auto<WsObj>().toLens()
class ChatWebSocketServer(
    scope: CoroutineScope,
) {

    private val chatPath = Path.of("chat")

    private val mutWsObjFlow = MutableSharedFlow<WsObj>()
    val wsObjFlow = mutWsObjFlow.asSharedFlow()

    private val clientSocketList: MutableList<Websocket> = mutableListOf()
    private val mapper = jacksonObjectMapper()

    private val ws = websockets {
        "/{chat}" bind { ws: Websocket ->
            chatPath(ws.upgradeRequest)
            ws.send(WsMessage("Connected"))

            // Add connection to client list used later when sending
            clientSocketList.add(ws)

            ws.suspendOnMessage(scope, mapper) { msg ->
                onReceived(msg)
            }
        }
    }.asServer(Undertow(serverPort))

    suspend fun send(wsObj: WsObj) {
        clientSocketList.forEach { ws ->
            ws.send(
                WsMessage(
                    mapper.writeValueAsString(wsObj)
                )
            )
        }
    }

    private suspend fun onReceived(wsObj: WsObj) {
        when(wsObj) {
            is ChatMessage -> mutWsObjFlow.emit(wsObj)
        }
    }
    fun startServer() {
        ws.start()
    }
    fun stopServer(){
        ws.stop()
    }
}

/**
 *  Listen for messages using given [CoroutineScope] and [Websocket.onMessage].
 *  For each message received a new coroutine is launched.
 *  Calls executable after converting the type using the JSON field type
 */
fun Websocket.suspendOnMessage(
    scope: CoroutineScope,
    mapper: ObjectMapper,
    executable: suspend (msg: WsObj) -> Unit
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
            executable(data.getOrElse { return@launch })
        }
    }
}



