package io.silv.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private const val dataStoreKey = "pref-data-store-key"

val Context.datastore: DataStore<Preferences> by preferencesDataStore(dataStoreKey)

interface EncryptedDatastore {

    suspend fun writeUserPasscode(pass: String)

    suspend fun readUserPasscode(): String
}

class EncryptedDatastoreImpl @Inject constructor(
    context: Context
): EncryptedDatastore {

    private val store = context.datastore

    private val  USER_PASS_KEY = stringPreferencesKey("USER_PASS_KEY")

    override suspend fun writeUserPasscode(pass: String) {
        store.edit { prefs ->
            AESEncryption.encrypt(pass)?.let { encryptedPass ->
                prefs[USER_PASS_KEY] = encryptedPass
            }
        }
    }

    override suspend fun readUserPasscode(): String =
        store.data.map { prefs ->
            prefs[USER_PASS_KEY]?.let { encryptedPass ->
                AESEncryption.decrypt(encryptedPass)
            } ?: ""
        }.first()

}