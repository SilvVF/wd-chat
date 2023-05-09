package io.silv.feature_create_group.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_create_group.CreateGroupViewModel
import io.silv.feature_create_group.use_case.CreateGroupUseCase
import io.silv.feature_create_group.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_create_group.use_case.createGroupUseCaseImpl
import io.silv.feature_create_group.use_case.observeWifiDirectEventsUseCaseImpl
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@Module
@InstallIn(ViewModelComponent::class)
object CreateGroupModule {

    @Provides
    @ViewModelScoped
    fun provideCreateGroupViewModel(
        createGroupUseCase: CreateGroupUseCase,
        observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase
    ): CreateGroupViewModel = CreateGroupViewModel(
        createGroupUseCase = createGroupUseCase,
        observeWifiDirectEventsUseCase = observeWifiDirectEventsUseCase,
    )

    @Provides
    @ViewModelScoped
    fun provideCreateGroupUseCase(
        p2p: P2p,
    ): CreateGroupUseCase = CreateGroupUseCase { groupInfo ->
        createGroupUseCaseImpl(
            p2p = p2p,
            passPhrase = groupInfo.passPhrase,
            networkName = groupInfo.networkName
        )
    }

    @Provides
    @ViewModelScoped
    fun provideObserveWifiDirectEventsUseCase(
        receiver: WifiP2pReceiver
    ) = ObserveWifiDirectEventsUseCase {
        observeWifiDirectEventsUseCaseImpl(receiver)
    }
}