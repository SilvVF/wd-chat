package io.silv.feature_search_users.di

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_search_users.SearchUsersViewModel
import io.silv.feature_search_users.use_case.ConnectToDeviceUseCase
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.StartDiscoveryUseCase
import io.silv.feature_search_users.use_case.connectToDeviceUseCaseImpl
import io.silv.feature_search_users.use_case.observeWifiDirectEventsUseCaseImpl
import io.silv.feature_search_users.use_case.startDiscoveryUseCaseImpl
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pReceiver


@Module
@InstallIn(ViewModelComponent::class)
object SearchUserModule {

    @Provides
    fun provideSearchUsersViewModel(
        wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
        connectToDeviceUseCase: ConnectToDeviceUseCase,
        startDiscoveryUseCase: StartDiscoveryUseCase,
    ): SearchUsersViewModel = SearchUsersViewModel(
        wifiDirectEventsUseCase = wifiDirectEventsUseCase,
        connectToDeviceUseCase = connectToDeviceUseCase,
        startDiscovery = startDiscoveryUseCase
    )

    @Provides
    fun provideP2p(
        @ApplicationContext context: Context,
        wifiP2pManager: WifiP2pManager
    ): P2p {
        return P2p.getImpl(context, wifiP2pManager)
    }


    @ViewModelScoped
    @Provides
    fun provideConnectToDeviceUseCase(
        p2p: P2p,
    ): ConnectToDeviceUseCase = ConnectToDeviceUseCase { wifiP2pDevice, wifiP2pConfig ->
        connectToDeviceUseCaseImpl(p2p, wifiP2pDevice, wifiP2pConfig)
    }

    @ViewModelScoped
    @Provides
    fun provideStartDiscoveryUseCase(
        p2p: P2p,
    ): StartDiscoveryUseCase = StartDiscoveryUseCase {
        startDiscoveryUseCaseImpl(p2p)
    }


    @ViewModelScoped
    @Provides
    fun provideObserveWifiDirectEventsUseCase(
        p2pReceiver: WifiP2pReceiver,
    ): ObserveWifiDirectEventsUseCase = ObserveWifiDirectEventsUseCase {
        observeWifiDirectEventsUseCaseImpl(p2pReceiver)
    }

}