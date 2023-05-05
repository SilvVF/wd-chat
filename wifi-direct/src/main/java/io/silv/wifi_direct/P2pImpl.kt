package io.silv.wifi_direct

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Looper
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow


internal class P2pImpl(
    ctx: Context,
    private val wifiP2pManager: WifiP2pManager,
): P2p, P2pCallbacks {


    private val channel: WifiP2pManager.Channel =
        wifiP2pManager.initialize(ctx, Looper.getMainLooper()) {
            //handle disconnect
        }

    override val peersFlow: Flow<List<WifiP2pDevice>> = peerListListenerCallbackFlow
    override val groupInfoFlow: Flow<WifiP2pGroup> = groupInfoListenerCallbackFlow

    override fun startDiscovery(): Flow<Either<P2pError, Boolean>> = flow {
        emit(discoverDevicesCallbackFlow.first())
    }

    override fun connect(
        device: WifiP2pDevice,
        config: WifiP2pConfig.Builder.() -> Unit
    ): Flow<Either<P2pError, Boolean>> = flow {
        emit(
            connectCallbackFlow(
                WifiP2pConfig.Builder()
                    .apply(config)
                    .build()
            )
                .first()
        )
    }

    @SuppressLint("MissingPermission")
    private val discoverDevicesCallbackFlow = callbackFlow {
        Either.catch {
            wifiP2pManager.discoverPeers(this@P2pImpl.channel, object: ActionListener {
                override fun onSuccess() {
                    trySend(Either.Right(true))
                }

                override fun onFailure(p0: Int) {
                    trySend(Either.Left(P2pError.GenericError("$p0")))
                }
            })
        }.mapLeft { t ->
            Either.Left(P2pError.MissingPermission("$t"))
        }
        awaitCancellation()
    }

    @SuppressLint("MissingPermission")
    private fun connectCallbackFlow(cfg: WifiP2pConfig) = callbackFlow<Either<P2pError, Boolean>> {
        Either.catch {
            wifiP2pManager.connect(
                this@P2pImpl.channel, cfg,
                object : ActionListener {
                    override fun onSuccess() {
                        trySend(Either.Right(true))
                    }
                    override fun onFailure(p0: Int) {
                        trySend(Either.Left(P2pError.GenericError("$p0")))
                    }
                }
            )
        }.mapLeft { t ->
            Either.Left(P2pError.MissingPermission(t.message ?: ""))
        }
        awaitCancellation()
    }
}
