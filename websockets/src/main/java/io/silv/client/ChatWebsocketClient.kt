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
import kotlinx.coroutines.cancel
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

    private  var webSocketSession: DefaultWebSocketSession? = null

    private val httpClient = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }

    fun start() = scope.launch {  startClient() }

    private suspend fun startClient() = httpClient.webSocket(
        host = address,
        port = serverPort,
        path = "/chat"
    ) {
        webSocketSession = this
        try {
            while (true) {
                val data = receiveDeserialized<WsData>()
                mutWsDataFlow.emit(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
           webSocketSession = null
           cancel()
        }
    }

    override suspend fun send(wsData: WsData) {
        webSocketSession?.send(
            Frame.Text(
                Json.encodeToString(wsData)
            )
        )
    }
}