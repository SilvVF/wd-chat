package io.silv.feature_chat.types

import android.net.Uri
import androidx.compose.runtime.Immutable
import io.silv.ChatMessage
import io.silv.UserInfo
import io.silv.WsData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


suspend fun WsData.toDomain(
    byteArrayToUri: suspend (ByteArray, String) -> Uri
): UiWsData = when (this) {
        is ChatMessage -> {
            UiChat(
                message = Message(
                    author = this.sender,
                    content = this.message,
                    images = this.images.map { byteArrayToUri(it.data, it.ext) },
                    authorId = this.id
                ),
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

interface Chat {
    val message: Message
}

@Immutable
data class Message(
    val author: String,
    val authorId: String,
    val content: String,
    val timestamp: String = LocalDateTime.now().format(
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    ).toString(),
    val images: List<Uri> = emptyList(),
)


sealed class UiWsData(
    open val id: String
)

data class UiChat(
    override val message: Message,
//    val date: LocalDateTime,
    override val id: String,
): UiWsData(id), Chat

data class MyChat(
    override val message: Message,
): UiWsData("me"), Chat

data class UiUserInfo(
    val name: String,
    val icon: Uri,
    override val id: String,
): UiWsData(id)