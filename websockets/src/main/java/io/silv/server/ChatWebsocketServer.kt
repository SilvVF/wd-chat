package io.silv.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.silv.ChatMessage
import io.silv.WsObj
import io.silv.serverPort
import io.silv.suspendOnMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.websockets

import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage


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
            // Add connection to client list used later when sending
            clientSocketList.add(ws)

            ws.send(WsMessage("Connected"))


            ws.suspendOnMessage(scope, mapper, ws) { wsObj, ws ->
                onReceived(wsObj)

                // send message to all others connected
                sendToOthers(
                    data = wsObj,
                    sender = ws
                )
            }
        }
    }.asServer(Undertow(serverPort))


    suspend fun send(data: WsObj) {
        clientSocketList.forEach { ws ->
            ws.send(
                WsMessage(
                    mapper.writeValueAsString(data)
                )
            )
        }
    }
    private suspend fun sendToOthers(data: WsObj, sender: Websocket) {
        clientSocketList
            .filter { it != sender }
            .forEach { ws ->
                ws.send(
                    WsMessage(
                        mapper.writeValueAsString(data)
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




