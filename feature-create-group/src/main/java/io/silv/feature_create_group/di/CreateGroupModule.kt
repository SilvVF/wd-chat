package io.silv.feature_create_group.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.silv.feature_create_group.CreateGroupViewModel
import io.silv.feature_create_group.use_case.CreateGroupUseCase
import io.silv.feature_create_group.use_case.createGroupUseCaseImpl
import io.silv.wifi_direct.P2p
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@Module
@InstallIn(ViewModelComponent::class)
object CreateGroupModule {

    @Provides
    @ViewModelScoped
    fun provideCreateGroupViewModel(
        createGroupUseCase: CreateGroupUseCase
    ): CreateGroupViewModel = CreateGroupViewModel(
        createGroupUseCase = createGroupUseCase
    )

    @Provides
    @ViewModelScoped
    fun provideCreateGroupUseCase(
        p2p: P2p,
       // chatWebSocketHandler: ChatWebSocketHandler
    ): CreateGroupUseCase = CreateGroupUseCase { groupInfo ->
        createGroupUseCaseImpl(
            p2p = p2p,
            scope = CoroutineScope(Dispatchers.Default),
            passPhrase = groupInfo.passPhrase,
            networkName = groupInfo.networkName
        )
    }
}