package io.silv.feature_chat.repo

import io.silv.SendReceive
import io.silv.WsData
import io.silv.client.ChatWebsocketClient
import io.silv.image_store.ImageRepository
import io.silv.server.ChatWebsocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WebsocketRepo (
    private val scope: CoroutineScope,
) {

    private  var ws: SendReceive? = null

    private val closeActions = mutableListOf<() -> Unit>()

    fun getReceiveFlow(): Flow<WsData> = flow {
        ws?.wsDataFlow?.collect { wsObj ->
            emit(wsObj)
        }
    }

    suspend fun startConnection(
        groupOwner: Boolean,
        groupOwnerAddress: String,
    ){
        ws = if (groupOwner) {
           ChatWebsocketServer(scope).also {
                val job = it.start()
                closeActions.add { job.cancel() }
            }
        } else {
            ChatWebsocketClient(groupOwnerAddress, scope).also { wsClient ->
                var clientJob: Job? = null
                for (i in 0..5) {
                    delay(1500)
                    runCatching { clientJob = wsClient.start() }
                    break
                }
                closeActions.add { clientJob?.cancel() }
            }
        }
    }
    suspend fun send(wsData: WsData) {
        ws?.send(wsData)
    }

    fun stopConnection() {
        closeActions.forEach { action ->
            action.invoke()
        }
    }
}