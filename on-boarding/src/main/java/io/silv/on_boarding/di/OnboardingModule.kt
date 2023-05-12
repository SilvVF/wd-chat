package io.silv.on_boarding.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.datastore.EncryptedDatastore
import io.silv.image_store.ImageRepository
import io.silv.on_boarding.OnboardViewModel

@Module
@InstallIn(ViewModelComponent::class)
object OnboardModule {

    @Provides
    @ViewModelScoped
    fun provideOnboardViewModel(
        encryptedDatastore: EncryptedDatastore,
        imageRepository: ImageRepository
    ): OnboardViewModel = OnboardViewModel(
        datastore = encryptedDatastore,
        imageRepository
    )
}