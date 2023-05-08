package io.silv.feature_create_group.use_case

import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal suspend fun createGroupUseCaseImpl(
    p2p: P2p,
    scope: CoroutineScope,
    passPhrase: String,
    networkName: String
): Either<P2pError, Boolean> {
    return p2p.createGroup(
        passPhrase,
        networkName
    )
}
