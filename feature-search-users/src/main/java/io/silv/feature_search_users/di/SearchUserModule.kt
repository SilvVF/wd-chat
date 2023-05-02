package io.silv.feature_search_users.di

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_search_users.SearchUsersViewModel
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.SearchUsersUseCase
import io.silv.feature_search_users.use_case.observeWifiDirectEventsUseCaseImpl
import io.silv.feature_search_users.use_case.searchUsersUseCaseImpl
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pReceiver


@Module
@InstallIn(ViewModelComponent::class)
object SearchUserModule {

    @Provides
    fun provideSearchUsersViewModel(
        searchUsersUseCase: SearchUsersUseCase,
        observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase
    ): SearchUsersViewModel = SearchUsersViewModel(
        searchUsersUseCase = searchUsersUseCase,
        wifiDirectEventsUseCase = observeWifiDirectEventsUseCase
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
    fun provideSearchUsersUseCase(
        p2p: P2p,
    ): SearchUsersUseCase = SearchUsersUseCase {
        searchUsersUseCaseImpl(p2p)
    }


    @ViewModelScoped
    @Provides
    fun provideObserveWifiDirectEventsUseCase(
        p2pReceiver: WifiP2pReceiver,
    ): ObserveWifiDirectEventsUseCase = ObserveWifiDirectEventsUseCase {
        observeWifiDirectEventsUseCaseImpl(p2pReceiver)
    }

}