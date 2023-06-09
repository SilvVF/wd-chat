package io.silv.wifidirectchat.di

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import androidx.core.content.ContextCompat.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.silv.datastore.EncryptedDatastore
import io.silv.image_store.ImageRepository
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifidirectchat.MainActivityViewModel
import io.silv.wifidirectchat.use_case.ObserveWifiDirectEventsUseCase
import io.silv.wifidirectchat.use_case.observeWifiDirectEventsUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideWifiP2pManager(
         @ApplicationContext context: Context
    ): WifiP2pManager {
        return getSystemService(context, WifiP2pManager::class.java) as WifiP2pManager
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        @ApplicationContext context: Context
    ): ImageRepository = ImageRepository.getImpl(context)



    @Provides
    @Singleton
    fun provideWifiP2pReceiver(
        @ApplicationContext context: Context,
    ): WifiP2pReceiver = WifiP2pReceiver(context)

    @Provides
    @Singleton
    fun provideObserveWifiDirectEventsUseCase(
        receiver: WifiP2pReceiver
    ) = ObserveWifiDirectEventsUseCase {
        observeWifiDirectEventsUseCaseImpl(receiver)
    }

    @Provides
    @Singleton
    fun provideMainActivityViewModel(
        observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
        datastore: EncryptedDatastore
    ): MainActivityViewModel {
        return MainActivityViewModel(
            observeWifiDirectEventsUseCase = observeWifiDirectEventsUseCase,
            datastore = datastore
        )
    }
}
