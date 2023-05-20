package io.silv.server

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import io.silv.SendReceive
import io.silv.WsData
import io.silv.json
import io.silv.serverPort
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.Collections

class ChatWebsocketServer(
    private val scope: CoroutineScope,
): SendReceive {

    private val mutWsDataFlow = MutableSharedFlow<WsData>()
    override val wsDataFlow = mutWsDataFlow.asSharedFlow()

    private val connections: MutableSet<DefaultWebSocketSession> =
        Collections.synchronizedSet(LinkedHashSet())

    fun start() = scope.launch {
        runCatching {
            embeddedServer(
                factory = Netty,
                port = serverPort,
            ) {
                install(CallLogging) {
                    level = Level.INFO
                }
                install(Routing)
                serverWebSockets()
            }.start(true)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun Application.serverWebSockets() {

        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }

        routing {
            webSocket("/chat") {
                connections += this
                try {
                    while (true) {
                        val data = receiveDeserialized<WsData>()
                        mutWsDataFlow.emit(data)
                        sendToOthers(data, this)
                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("onClose ${closeReason.await()}")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    connections -= this
                }
            }
        }
    }

    override suspend fun send(wsData: WsData) {
        connections.forEach { session ->
            session.send(
                Frame.Text(Json.encodeToString(wsData))
            )
        }
    }
    private suspend fun sendToOthers(data: WsData, sender: DefaultWebSocketSession) {
        connections.forEach { wsSession ->
            if (wsSession == sender) {
                return@forEach
            }
            wsSession.send(
                Frame.Text(Json.encodeToString(data))
            )
        }
    }
}




