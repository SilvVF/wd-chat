package io.silv.wifi_direct

import android.annotation.SuppressLint
import android.content.Context
import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import io.silv.wifi_direct.util.locationPerms
import io.silv.wifi_direct.util.nearbyDevicePerms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class P2pImpl(
    private val ctx: Context,
    private val wifiP2pManager: WifiP2pManager,
): P2p {

    private val channel: WifiP2pManager.Channel =
        wifiP2pManager.initialize(ctx, Looper.getMainLooper()) {
            //handle disconnect
        }


    @SuppressLint("MissingPermission")
    override fun getNearbyDevices(): Flow<Either<List<WifiP2pDevice>, P2pError>> = flow {
        if (locationPerms(ctx) && nearbyDevicePerms(ctx)) {
            wifiP2pManager.discoverPeers(
                channel,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                            wifiP2pManager.requestPeers(channel) { peers ->
                                suspend {
                                    emit(Either.Left(peers.deviceList.toList()))
                                }}
                    }
                    override fun onFailure(p0: Int) {
                        suspend {
                            emit(Either.Right(P2pError.GenericError("Failed to Discover")))
                        }
                    }
                })
        } else { emit(Either.Right(P2pError.MissingPermission(""))) }
    }.flowOn(Dispatchers.IO)

    override fun setResultForMAC(
        mac: MacAddress,
        shouldAccept: () -> Boolean
    ): Flow<Either<Boolean, P2pError>> {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(
        device: WifiP2pDevice,
        config: WifiP2pConfig.Builder.() -> Unit
    ): Flow<Either<WifiP2pInfo, P2pError>> = flow {
        p2pMangerConnectCallbackFlow(
            WifiP2pConfig.Builder()
                .apply(config)
                .build()
        ).collect { result ->
            when (result) {
                is Either.Left -> {
                    requestInfoCallbackFlow.first()?.let { info ->
                        emit(Either.Left(info))
                    } ?: emit(Either.Right(P2pError.GenericError("Could not get info")))
                }
                is Either.Right -> emit(Either.Right(result.value))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun requestInfo(): Flow<Either<WifiP2pInfo, P2pError>> {
        TODO("Not yet implemented")
    }


    private val requestInfoCallbackFlow = callbackFlow<WifiP2pInfo?> {
        wifiP2pManager.requestConnectionInfo(this@P2pImpl.channel) { info ->
            trySend(info)
        }
    }


    @SuppressLint("MissingPermission")
    private fun p2pMangerConnectCallbackFlow(cfg: WifiP2pConfig) = callbackFlow<Either<Boolean, P2pError>> {
        if (locationPerms(ctx) && nearbyDevicePerms(ctx)) {
            wifiP2pManager.connect(
                this@P2pImpl.channel, cfg,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        trySend(Either.Left(true))
                    }
                    override fun onFailure(p0: Int) {
                        trySend(Either.Left(false))
                    }
                }
            )
        } else {
            Either.Right(
                P2pError.MissingPermission(
                    "" // TODO("Get Permsissions")
                )
            )
        }
    }

}
