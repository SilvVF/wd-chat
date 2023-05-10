package io.silv

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZoneOffset

@Serializable
sealed class WsData(
    @Serializable(LocalDateTimeSerializer::class)
    val date: LocalDateTime = LocalDateTime.now()
)

@Serializable
@SerialName(ChatMessage.typeName)
data class ChatMessage(
    val message: String,
    val name: String,
    val address: String,
    val images: List<Image> = emptyList()
): WsData() {

    companion object {
        const val typeName = "chat"
    }
}

@Serializable
@SerialName(Image.typeName)
data class Image(
    val data: ByteArray,
): WsData() {

    companion object {
        const val typeName = "image"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}






object LocalDateTimeSerializer : KSerializer<LocalDateTime> {

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    private val offset = ZoneOffset.of(
        ZoneOffset.systemDefault().toString()
    )
    override fun serialize(encoder: Encoder, value: LocalDateTime)  {
        encoder.encodeLong(value.toEpochSecond(offset))
    }
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.ofEpochSecond(
            decoder.decodeLong(),
            0,
            offset
        )
    }
}