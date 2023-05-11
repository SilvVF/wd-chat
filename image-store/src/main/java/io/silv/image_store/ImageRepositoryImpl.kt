package io.silv.image_store

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

internal class ImageRepositoryImpl(
    private val context: Context,
    private val dirName: String,
): ImageRepository {

    private val fileProviderAuthority = "io.silv.image_store.chat_attachments.fileprovider"

    private val typeMap = MimeTypeMap.getSingleton()
    private val cr = context.contentResolver

    private val dir = File(context.filesDir, dirName)

    private val mutex = Mutex()

    init {
        CoroutineScope(Dispatchers.IO).launch { clear() }
    }

    override fun getExtFromUri(uri: Uri) =
        typeMap.getExtensionFromMimeType(cr.getType(uri)) ?: ""

    /**
     * @throws
     * Locks [mutex] while performing the write operation other methods in the [ImageRepository]
     * interface can not be called during this.
     * Multiple calls to write can happen at the same time and this function does not wait
     * on the mutex to unlock.
     * @return New [Uri] for the file after writing it to internal File storage
     */
    override suspend fun write(uri: Uri): Uri  {
        mutex.tryLock()
        val ext = getExtFromUri(uri)
        val fileName = "$dirName-${UUID.randomUUID()}.$ext"
        val attachment = File(dir, fileName)
        dir.mkdirs()
        cr.openInputStream(uri).use { stream ->
            stream?.let {
                attachment.writeBytes(stream.readBytes())
            }
        }
        return getUriFromFile(attachment).also {
            runCatching { mutex.unlock() }
            Log.d("IMAGE_REPOSITORY", "saved content original uri $uri, new uri $it")
        }
    }

    override suspend fun write(byteArray: ByteArray, ext: String): Uri {
        mutex.tryLock()
        val fileName = "$dirName-${UUID.randomUUID()}.$ext"
        val image = File(dir, fileName)
        dir.mkdirs()
        image.writeBytes(byteArray)
        return getUriFromFile(image).also {
            runCatching { mutex.unlock() }
            Log.d("IMAGE_REPOSITORY", "saved content from ByteArray ext:$ext, new uri $it")
        }
    }


    /**
     * Reads all [Uri]'s in the File Storage.
     * Waits for the [mutex] to be unlocked and locks it while reading.
     */
    override suspend fun readAll(): List<Uri> = mutex.withLock {
        buildList {
            dir.listFiles { file ->
                add(getUriFromFile(file))
            }
        }
    }

    /**
     * Deletes file for the given [Uri].
     * @return true if the file is not found or deleted successfully otherwise false.
     */
    override suspend fun delete(uri: Uri): Boolean = mutex.withLock {
        dir.listFiles { file -> getUriFromFile(file) == uri }
            .ifEmpty {
                return true
            }.onEach { match ->
                 if(!match.delete()) {
                     return false
                 }
        }
        return true
    }

    /**
     * Deletes all files in the directory.
     * @return true if all files were deleted otherwise false.
     */
    override suspend fun clear(): Boolean = mutex.withLock {
        dir.listFiles()?.onEach {
            if(!it.delete()) {
                return false
            }
        }
        return true
    }

    override suspend fun getFileFromUri(uri: Uri): File? {
        return dir.listFiles { file ->
            getUriFromFile(file) == uri
        }
            ?.firstOrNull()
    }
    private fun getUriFromFile(file: File): Uri =
        FileProvider.getUriForFile(context, fileProviderAuthority, file)

}