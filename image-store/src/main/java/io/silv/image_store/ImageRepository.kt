package io.silv.image_store

import android.content.Context
import android.net.Uri

interface ImageRepository {

    suspend fun readAll(): List<Uri>

    suspend fun write(uri: Uri): Uri

    suspend fun writeAll(vararg uris: Uri) = uris.map { uri -> write(uri) }

    suspend fun writeAll(uris: List<Uri>) = uris.map { uri -> write(uri) }

    suspend fun delete(uri: Uri): Boolean

    suspend fun deleteAll(vararg uris: Uri) = uris.map { delete(it) }.all { true }

    suspend fun deleteAll(uris: List<Uri>) = uris.map { delete(it) }.all { true }

    suspend fun clear(): Boolean

    companion object {
        fun getImpl(context: Context): ImageRepository =
            ImageRepositoryImpl(context, "chat_attachments")
    }
}