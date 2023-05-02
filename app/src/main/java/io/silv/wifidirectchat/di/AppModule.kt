package io.silv.wifidirectchat.di

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import androidx.core.content.ContextCompat.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import io.silv.feature_search_users.SearchUsersViewModel
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.SearchUsersUseCase
import io.silv.on_boarding.OnboardViewModel
import io.silv.wifi_direct.WifiP2pReceiver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOnboardViewModel(): OnboardViewModel = OnboardViewModel()


    @Provides
    @Singleton
    fun provideWifiP2pManager(
         @ApplicationContext context: Context
    ): WifiP2pManager {
        return getSystemService(context, WifiP2pManager::class.java) as WifiP2pManager
    }

    @Provides
    @Singleton
    fun provideWifiP2pReceiver(): WifiP2pReceiver = WifiP2pReceiver()

}
