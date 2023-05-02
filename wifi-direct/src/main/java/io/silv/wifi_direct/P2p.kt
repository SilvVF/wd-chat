package io.silv.wifi_direct

import android.content.Context
import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow

interface P2p {

    fun getNearbyDevices(): Flow<Either<List<WifiP2pDevice>, P2pError>>

    fun setResultForMAC(macAddr: MacAddress, shouldAccept: () -> Boolean): Flow<Either<Boolean, P2pError>>

    suspend fun connect(device: WifiP2pDevice,  config: WifiP2pConfig.Builder.() -> Unit = {}): Flow<Either<WifiP2pInfo, P2pError>>

    suspend fun requestInfo(): Flow<Either<WifiP2pInfo, P2pError>>

    companion object {
        fun getImpl(ctx: Context, p2pManager: WifiP2pManager): P2p = P2pImpl(ctx, p2pManager)
    }
}

