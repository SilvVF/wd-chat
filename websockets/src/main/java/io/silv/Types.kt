package io.silv

import java.time.LocalDateTime

abstract class WsObj(val type: String)

data class ChatMessage(
    val message: String,
    val name: String,
    val address: String,
    val timeSent: LocalDateTime
): WsObj(typeName) {

   companion object {
       const val typeName = "ChatMessage"
   }
}



