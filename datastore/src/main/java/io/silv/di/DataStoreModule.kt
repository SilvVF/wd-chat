package io.silv.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.silv.datastore.EncryptedDatastore
import io.silv.datastore.EncryptedDatastoreImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun  provideEncryptedDataStore(
        @ApplicationContext context: Context
    ): EncryptedDatastore {
        return EncryptedDatastoreImpl(context)
    }
}