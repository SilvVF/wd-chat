package io.silv.wifi_direct.types

sealed class P2pError(val message: String) {
    class GenericError(message: String) : P2pError(message)
    data class MissingPermission(val permissions: String): P2pError(permissions)
}