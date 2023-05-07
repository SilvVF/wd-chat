package io.silv.feature_chat.repo

import io.silv.SendReceive
import io.silv.WsObj
import io.silv.client.ChatWebsocketClient
import io.silv.server.ChatWebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WebsocketRepo (
    private val scope: CoroutineScope
) {

    private lateinit var ws: SendReceive

    private val closeActions = mutableListOf<() -> Unit>()

    fun getReceiveFlow(): Flow<WsObj> = flow {
        ws.wsObjFlow.collect { wsObj ->
            emit(wsObj)
        }
    }

    fun startConnection(
        groupOwner: Boolean,
        groupOwnerAddress: String,
    ){
        ws = if (groupOwner) {
           ChatWebSocketServer(scope).also {
                it.startServer()
                closeActions.add { it.stopServer() }
            }
        } else {
            ChatWebsocketClient(groupOwnerAddress, scope)
        }
    }

    suspend fun send(wsObj: WsObj) {
        ws.send(wsObj)
    }

    fun stopConnection() {
        closeActions.forEach { action ->
            action.invoke()
        }
    }
}