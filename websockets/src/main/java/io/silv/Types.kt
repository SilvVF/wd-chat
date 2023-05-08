package io.silv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WsData

@Serializable
@SerialName(ChatMessage.typeName)
data class ChatMessage(
    val message: String,
    val name: String,
    val address: String,

): WsData() {

    companion object {
        const val typeName = "chat"
    }
}




