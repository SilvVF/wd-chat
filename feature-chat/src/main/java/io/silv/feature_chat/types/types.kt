package io.silv.feature_chat.types

import android.net.Uri
import io.silv.ChatMessage
import io.silv.UserInfo
import io.silv.WsData


suspend fun WsData.toDomain(
    byteArrayToUri: suspend (ByteArray, String) -> Uri
): UiWsData = when (this) {
        is ChatMessage -> {
            UiChat(
                message = this.message,
                sender = this.sender,
                images = this.images.map { byteArrayToUri(it.data, it.ext) },
//                date = this.date,
                id = this.id
            )
        }
        is UserInfo -> {
            UiUserInfo(
                name = this.name,
                icon = byteArrayToUri(this.icon.data, this.icon.ext),
                id = this.id
            )
        }
    }

sealed interface Chat

sealed class UiWsData(
    open val id: String
)

data class UiChat(
    val message: String,
    val sender: String,
    val images: List<Uri>,
//    val date: LocalDateTime,
    override val id: String,
): UiWsData(id), Chat

data class MyChat(
    val message: String,
    val images: List<Uri>
): UiWsData("me"), Chat

data class UiUserInfo(
    val name: String,
    val icon: Uri,
    override val id: String,
): UiWsData(id)