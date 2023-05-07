package io.silv.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val dataStoreKey = "pref-data-store-key"

val Context.datastore: DataStore<Preferences> by preferencesDataStore(dataStoreKey)

interface EncryptedDatastore {

    suspend fun writeUserPasscode(pass: String)

    suspend fun readUserPasscode(): Flow<String?>

    suspend fun writeUserName(name: String)

    suspend fun readUserName(): Flow<String?>

}

class EncryptedDatastoreImpl @Inject constructor(
    context: Context
): EncryptedDatastore {

    private val store = context.datastore

    private val USER_PASS_KEY = stringPreferencesKey("USER_PASS_KEY")
    private val USER_NAME_KEY = stringPreferencesKey("USER_NAME_KEY")

    override suspend fun writeUserPasscode(pass: String) {
        store.edit { prefs ->
            AESEncryption.encrypt(pass)?.let { encryptedPass ->
                prefs[USER_PASS_KEY] = encryptedPass
            }
        }
    }

    override suspend fun readUserPasscode(): Flow<String?> =
        store.data.map { prefs ->
            prefs[USER_PASS_KEY]?.let { encryptedPass ->
                AESEncryption.decrypt(encryptedPass)
            }
        }

    override suspend fun writeUserName(name: String) {
        store.edit { prefs ->
            AESEncryption.encrypt(name)?.let { encryptedName ->
                prefs[USER_NAME_KEY] = encryptedName
            }
        }
    }

    override suspend fun readUserName(): Flow<String?> =
        store.data.map { prefs ->
            prefs[USER_NAME_KEY]?.let { encryptedName ->
                AESEncryption.decrypt(encryptedName)
            }
        }

}