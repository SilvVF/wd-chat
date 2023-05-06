package io.silv.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.silv.WsObj
import io.silv.serverPort
import io.silv.suspendOnMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

class ChatWebsocketClient(
    private val address: String,
    scope: CoroutineScope,
) {

    private val mutWsObjFlow = MutableSharedFlow<WsObj>()
    val wsObjFlow = mutWsObjFlow.asSharedFlow()

    private val mapper = jacksonObjectMapper()


    private val client = WebsocketClient.nonBlocking(
        Uri.of("ws://$address:$serverPort/chat")
    ) { ws ->
        ws.run {
            send(WsMessage("Connected"))
        }
    }

    init {
        client.suspendOnMessage(scope, mapper, client) { wsObj, ws ->
            mutWsObjFlow.emit(wsObj)
        }
    }

    fun send(wsObj: WsObj) {
        client.send(
            WsMessage(
                mapper.writeValueAsString(wsObj)
            )
        )
    }
}