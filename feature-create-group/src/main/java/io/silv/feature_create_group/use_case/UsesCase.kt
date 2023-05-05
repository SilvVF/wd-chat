package io.silv.feature_create_group.use_case


import arrow.core.Either
import io.silv.wifi_direct.types.P2pError

data class GroupInfo(
    val networkName: String,
    val passPhrase: String
)

fun interface CreateGroupUseCase: suspend (GroupInfo) -> Either<P2pError, Boolean>