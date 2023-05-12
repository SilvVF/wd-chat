package io.silv.feature_chat.use_case

import android.net.Uri
import arrow.core.Either
import io.silv.ChatMessage
import io.silv.Image
import io.silv.datastore.EncryptedDatastore
import io.silv.feature_chat.repo.WebsocketRepo
import io.silv.feature_chat.types.Message
import io.silv.feature_chat.types.MyChat
import io.silv.feature_chat.types.UiWsData
import io.silv.feature_chat.types.toDomain
import io.silv.image_store.ImageRepository
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal suspend fun writeToAttachmentsUseCaseImpl(
    ir: ImageRepository,
    uri: Uri
): Uri {
    return ir.write(uri)
}

internal suspend fun deleteAttachmentUseCaseImpl(uri: Uri, imageRepository: ImageRepository) {
    imageRepository.delete(uri)
}

internal suspend fun sendChatUseCaseImpl(
    websocketRepo: WebsocketRepo,
    ir: ImageRepository,
    message: String,
    uris: List<Uri>
): MyChat {
    val images = withContext(Dispatchers.IO) {
        uris.map {
            async {
                ir.getFileFromUri(it) to ir.getExtFromUri(it)
            }
        }
            .awaitAll()
            .mapBothNotNull { (file, ext) ->
                file?.readBytes() to ext
            }
            .map { (bytes, ext) ->
                Image(data = bytes, ext = ext)
            }
    }
    val chat = ChatMessage(
        message = message,
        images = images,
        sender = "name",
    )
    runCatching {
        websocketRepo.send(chat)
    }.onFailure {
        it.printStackTrace()
    }
    return MyChat(
        message = Message(
            author = "me",
            content = message,
            images = uris,
            authorId = "me"
        )
    )
}
internal suspend fun connectToChatUseCaseImpl(
    isGroupOwner: Boolean,
    groupOwnerAddress: String,
    websocketRepo: WebsocketRepo,
    datastore: EncryptedDatastore,
    ir: ImageRepository,
): Boolean {
    runCatching {

        val icon = datastore.readProfilePictureUri().first()?.let {
            ir.getFileFromUri(it)?.readBytes() to ir.getExtFromUri(it)
        }

        websocketRepo.startConnection(
            groupOwner = isGroupOwner,
            groupOwnerAddress = groupOwnerAddress,
            name = datastore.readUserName().first(),
            icon = icon
        )
    }
        .onFailure {
            it.printStackTrace()
        }
    return true
}

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): SharedFlow<WifiP2pEvent> {
    return wifiP2pReceiver.eventBroadcast
}

internal fun collectChatUseCaseImpl(
    websocketRepo: WebsocketRepo,
    imageRepository: ImageRepository
): Either<Throwable, Flow<UiWsData>> {

    return Either.catch {
        websocketRepo.getReceiveFlow().map {
            it.toDomain { bytes, ext ->
                imageRepository.writeChat(bytes, ext)
            }
        }
    }
}

fun <T, T2, R, R2>  List<Pair<T, T2>>.mapBothNotNull(transform: (Pair<T, T2>) -> Pair<R?, R2?>): List<Pair<R, R2>> {
    return this.mapNotNull {
        val (x, y) = transform(it)
        if (x == null || y == null)
            return@mapNotNull null
        else
            x to y
    }
}