package io.silv.wifi_direct

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
import android.os.Looper
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow



internal class P2pImpl(
    ctx: Context,
    private val wifiP2pManager: WifiP2pManager,
): P2p {


    private val channel: WifiP2pManager.Channel =
        wifiP2pManager.initialize(ctx, Looper.getMainLooper()) {
            //handle disconnect
        }

    override suspend fun requestGroupInfo(): Either<P2pError, WifiP2pGroup> {
        return Either.catch {
            groupInfoCallback.first()
        }.mapLeft { throwable ->
            P2pError.GenericError(throwable.message ?: "")
        }
    }

    override suspend fun startDiscovery(): Either<P2pError, Boolean> {
        return Either.catch {
            discoverDevicesCallbackFlow.first()
        }.mapLeft {
            P2pError.GenericError(it.message ?: "unknown error")
        }
    }

    @SuppressLint("MissingPermission")
    private val groupInfoCallback = callbackFlow {
        wifiP2pManager.requestGroupInfo(this@P2pImpl.channel) { groupInfo ->
            trySend(groupInfo)
        }
        awaitCancellation()
    }
    override suspend fun connect(
        device: WifiP2pDevice,
        config: WifiP2pConfig.Builder.() -> Unit
    ): Either<P2pError, Boolean>  {
        return Either.catch {
            connectCallbackFlow(
                WifiP2pConfig.Builder()
                    .apply(config)
                    .build()
            ).first()
        }.mapLeft {
            P2pError.GenericError(it.message ?: "unknown error")
        }
    }

    override suspend fun createGroup(
        passPhrase: String,
        networkName: String
    ): Either<P2pError, Boolean> {
        return Either.catch {
            createGroupCallbackFlow(
                networkName = networkName,
                passPhrase = passPhrase
            ).first()
        }.mapLeft { throwable ->
            P2pError.GenericError(throwable.message ?: "")
        }
    }

    @SuppressLint("MissingPermission")
    private val discoverDevicesCallbackFlow = callbackFlow {
            wifiP2pManager.discoverPeers(this@P2pImpl.channel, object: ActionListener {
                override fun onSuccess() {
                    trySendBlocking(true)
                }

                override fun onFailure(p0: Int) {
                    close(cause = IllegalStateException())
                }
            })
        awaitClose()
    }

    private val createGroupPrefix = "DIRECT-xy"
    @SuppressLint("MissingPermission")
    private fun createGroupCallbackFlow(networkName: String, passPhrase: String) = callbackFlow {
        wifiP2pManager.createGroup(
            this@P2pImpl.channel,
            WifiP2pConfig.Builder().apply {
                setNetworkName(createGroupPrefix + networkName)
                setPassphrase(passPhrase)
            }.build(),
            object: ActionListener {
                override fun onSuccess() {
                    trySendBlocking(true)
                }
                override fun onFailure(p0: Int) {
                    close(IllegalStateException())
                }
        })
        awaitClose()
    }

    @SuppressLint("MissingPermission")
    private fun connectCallbackFlow(cfg: WifiP2pConfig) = callbackFlow {
            wifiP2pManager.connect(
                this@P2pImpl.channel, cfg,
                object : ActionListener {
                    override fun onSuccess() {
                        trySendBlocking(true)
                    }
                    override fun onFailure(p0: Int) {
                       close(IllegalStateException())
                    }
                }
            )
            awaitClose()
    }
}
