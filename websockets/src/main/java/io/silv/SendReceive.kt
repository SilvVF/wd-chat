package io.silv

import kotlinx.coroutines.flow.SharedFlow

interface SendReceive {

    val wsObjFlow: SharedFlow<WsObj>

    suspend fun send(wsObj: WsObj)
}