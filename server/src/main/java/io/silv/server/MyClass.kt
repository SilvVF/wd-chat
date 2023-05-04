package io.silv.server

import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.websockets
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import java.net.InetAddress

val chatPath = Path.of("chat")


class ChatWebSocketHandler(
    private val groupOwner: Boolean,
    private val groupOwnerAddress: InetAddress,
) {

    fun onMessage(msg: WsMessage) {

    }
}

fun webSockets(
     chatHandler: ChatWebSocketHandler
) = websockets {
    "/{chat}" bind { ws: Websocket ->
        val chat = chatPath(ws.upgradeRequest)
        ws.send(WsMessage("Connected"))

        ws.onMessage {
            chatHandler.onMessage(it)
        }
    }
}




