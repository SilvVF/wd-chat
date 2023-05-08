package io.silv

import kotlinx.coroutines.flow.SharedFlow

interface SendReceive {

    val wsDataFlow: SharedFlow<WsData>

    suspend fun send(wsData: WsData)
}