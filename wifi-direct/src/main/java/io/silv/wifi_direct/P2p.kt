package io.silv.wifi_direct

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

interface P2p {

    val peersFlow: Flow<List<WifiP2pDevice>>

    val groupInfoFlow: Flow<WifiP2pGroup>
    fun startDiscovery(): Flow<Either<P2pError, Boolean>>

    fun connect(device: WifiP2pDevice,  config: WifiP2pConfig.Builder.() -> Unit = {}): Flow<Either<P2pError, Boolean>>


    companion object {
        fun getImpl(ctx: Context, p2pManager: WifiP2pManager): P2p = P2pImpl(ctx, p2pManager)
    }
}

