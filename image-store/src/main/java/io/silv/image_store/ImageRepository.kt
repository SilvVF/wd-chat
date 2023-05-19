package io.silv.image_store

import android.content.Context
import android.net.Uri
import java.io.File

interface ImageRepository {

    suspend fun readAll(): List<Uri>

    suspend fun write(uri: Uri): Uri

    suspend fun writeProfilePicture(uri: Uri): Uri

    suspend fun writeChat(byteArray: ByteArray, ext: String): Uri

    suspend fun writeAll(vararg uris: Uri) = uris.map { uri -> write(uri) }

    suspend fun writeAll(uris: List<Uri>) = uris.map { uri -> write(uri) }

    suspend fun delete(uri: Uri): Boolean

    suspend fun deleteChats(uri: List<Uri>)

    suspend fun deleteAll(vararg uris: Uri) = uris.map { delete(it) }.all { true }

    suspend fun deleteAll(uris: List<Uri>) = uris.map { delete(it) }.all { true }

    suspend fun clear(): Boolean

    fun getExtFromUri(uri: Uri): String

    suspend fun getFileFromUri(uri: Uri): File?

    companion object {
        fun getImpl(context: Context): ImageRepository =
            ImageRepositoryImpl(context)
    }
}