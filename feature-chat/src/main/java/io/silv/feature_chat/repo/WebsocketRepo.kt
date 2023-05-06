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

    private var groupOwner = false
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
        runCatching {
            if (groupOwner) {
                this.groupOwner = true
                ws = ChatWebSocketServer(scope).also {
                    closeActions.add { it.startServer() }
                }
            } else {
                this.groupOwner = false
                ws = ChatWebsocketClient(groupOwnerAddress, scope)
            }
        }
    }

    suspend fun send(wsObj: WsObj) {
        ws.send(wsObj)
    }

    fun stopConnection() {
        runCatching {
            closeActions.forEach { action ->
                action.invoke()
            }
        }
    }
}