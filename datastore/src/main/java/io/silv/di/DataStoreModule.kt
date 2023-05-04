package io.silv.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.silv.datastore.EncryptedDatastore
import io.silv.datastore.EncryptedDatastoreImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Singleton
    @Binds
    abstract fun bindEncryptedDataStore(
        encryptedDatastore: EncryptedDatastoreImpl
    ): EncryptedDatastore
}