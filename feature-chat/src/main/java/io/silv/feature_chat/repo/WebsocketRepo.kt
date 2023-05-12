package io.silv.feature_chat.repo

import io.silv.Image
import io.silv.SendReceive
import io.silv.UserInfo
import io.silv.WsData
import io.silv.client.ChatWebsocketClient
import io.silv.server.ChatWebsocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.random.Random

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
        name: String?,
        icon: Pair<ByteArray?, String?>?
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
        sendUserInfo(name, icon)
    }

    private suspend fun sendUserInfo(name: String?, icon: Pair<ByteArray?, String?>?) = scope.launch {
        for (i in 0..3) {
            delay(2000)
            send(
                UserInfo(
                    name ?: "user-${Random.nextDouble()}",
                    Image(
                        icon?.first ?: ByteArray(0),
                        icon?.second ?: "")
                )
            )
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