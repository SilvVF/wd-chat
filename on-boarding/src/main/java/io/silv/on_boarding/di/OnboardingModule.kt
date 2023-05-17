package io.silv.on_boarding.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.datastore.EncryptedDatastore
import io.silv.image_store.ImageRepository
import io.silv.on_boarding.OnboardViewModel
import io.silv.on_boarding.use_case.CheckPermissionsGrantedUseCase
import io.silv.on_boarding.use_case.checkPermissionsGrantedUseCaseImpl

@Module
@InstallIn(ViewModelComponent::class)
object OnboardModule {

    @Provides
    @ViewModelScoped
    fun provideOnboardViewModel(
        savedStateHandle: SavedStateHandle,
        encryptedDatastore: EncryptedDatastore,
        imageRepository: ImageRepository,
        checkPermissionsGrantedUseCase: CheckPermissionsGrantedUseCase
    ): OnboardViewModel = OnboardViewModel(
        userStore = encryptedDatastore,
        imageRepository = imageRepository,
        savedStateHandle = savedStateHandle,
        checkPermissionsGrantedUseCase = checkPermissionsGrantedUseCase
    )

    @Provides
    @ViewModelScoped
    fun provideCheckPermissionsGrantedUseCase(
        @ApplicationContext context: Context
    ) = CheckPermissionsGrantedUseCase { permissions ->
        checkPermissionsGrantedUseCaseImpl(permissions, context)
    }
}