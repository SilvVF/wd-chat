package io.silv.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.silv.SendReceive
import io.silv.WsData
import io.silv.json
import io.silv.serverPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatWebsocketClient(
    private val address: String,
    private val scope: CoroutineScope,
): SendReceive {

    private val mutWsDataFlow = MutableSharedFlow<WsData>()
    override val wsDataFlow = mutWsDataFlow.asSharedFlow()

    private  var session: DefaultWebSocketSession? = null

    fun start() = scope.launch { startClient() }

    private suspend fun startClient() = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }.webSocket(
        host = address,
        port = serverPort,
        path = "/chat"
    ) {
        session = this
        val data = receiveDeserialized<WsData>()
        mutWsDataFlow.emit(data)
    }

    override suspend fun send(wsData: WsData) {
        session?.send(
            Frame.Text(
                Json.encodeToString(wsData)
            )
        )
    }
}