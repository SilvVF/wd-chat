package io.silv.wifi_direct

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

/**
 * @property peersFlow [Flow] of list [WifiP2pDevice]
 * @property groupInfoFlow [Flow] of [WifiP2pGroup]
 * @property startDiscovery [Flow] starts discovery process and emits Either [P2pError]
 * or true if discovery was started
 * @property connect [Flow] trys to connect to a [WifiP2pDevice] using [WifiP2pConfig] emits
 * Either [P2pError] or true if connection was successful
 */
interface P2p {

    suspend fun requestGroupInfo(): Either<P2pError, WifiP2pGroup>
    suspend fun startDiscovery(): Either<P2pError, Boolean>

    suspend fun connect(device: WifiP2pDevice,  config: WifiP2pConfig.Builder.() -> Unit = {}): Either<P2pError, Boolean>

    suspend fun requestDeviceList(): Either<P2pError, List<WifiP2pDevice>>

    suspend fun createGroup(
        passPhrase: String,
        networkName: String
    ):  Either<P2pError, Boolean>

    companion object {
        fun getImpl(ctx: Context, p2pManager: WifiP2pManager): P2p = P2pImpl(ctx, p2pManager)
    }
}

