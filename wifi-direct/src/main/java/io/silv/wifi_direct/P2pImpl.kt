package io.silv.wifi_direct

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Looper
import arrow.core.Either
import io.silv.wifi_direct.types.P2pError
import io.silv.wifi_direct.util.locationPerms
import io.silv.wifi_direct.util.nearbyDevicePerms
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first



internal class P2pImpl(
    private val ctx: Context,
    private val wifiP2pManager: WifiP2pManager,
): P2p {


    private val channel: WifiP2pManager.Channel =
        wifiP2pManager.initialize(ctx, Looper.getMainLooper()) {
            //handle disconnect
        }

    @Suppress("MissingPermissions")
    override suspend fun requestDeviceList(): Either<P2pError, List<WifiP2pDevice>> {
        return Either.catch {
            requestPeersCallbackFlow.first()
        }.mapLeft { throwable ->
            permissionsCheck()?.let {
                return@mapLeft it
            }
            P2pError.GenericError(throwable.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun requestGroupInfo(): Either<P2pError, WifiP2pGroup> {
        return Either.catch {
            groupInfoCallback.first()
        }.mapLeft { throwable ->
            permissionsCheck()?.let {
                return@mapLeft it
            }
            P2pError.GenericError(throwable.localizedMessage ?: "Unknown Error")
        }
    }

    override suspend fun startDiscovery(): Either<P2pError, Boolean> {
        return Either.catch {
            discoverDevicesCallbackFlow.first()
        }.mapLeft {
            permissionsCheck()?.let { return@mapLeft it }
            P2pError.GenericError(it.localizedMessage ?: "Unknown error")
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
                    .apply {
                        wps.setup = WpsInfo.PBC
                    }
            ).first()
        }.mapLeft {
            permissionsCheck()?.let { return@mapLeft it }
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
            permissionsCheck()?.let { return@mapLeft it }
            P2pError.GenericError(throwable.localizedMessage ?: "")
        }
    }


    @SuppressLint("MissingPermission")
    private val requestPeersCallbackFlow = callbackFlow {
        wifiP2pManager.requestPeers(this@P2pImpl.channel) { peers ->
            trySendBlocking(peers.deviceList.toList())
        }
        awaitClose()
    }

    @SuppressLint("MissingPermission")
    private val discoverDevicesCallbackFlow = callbackFlow {
            wifiP2pManager.discoverPeers(this@P2pImpl.channel, object: ActionListener {
                override fun onSuccess() {
                    trySendBlocking(true)
                }

                override fun onFailure(p0: Int) {
                    close(
                        cause = IllegalStateException(
                            getErrorMessage(p0)
                        )
                    )
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
                    close(
                        IllegalStateException(
                           getErrorMessage(p0)
                        )
                    )
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

    private fun getErrorMessage(code: Int) =  when(code) {
        WifiP2pManager.P2P_UNSUPPORTED -> "Wifi P2P UNSUPPORTED try enabling wifi p2p in settings"
        WifiP2pManager.ERROR ->  "Wifi P2P ERROR"
        WifiP2pManager.BUSY ->  "Wifi P2P BUSY try disconnecting from settings or restart app"
        else -> "Unknown Error"
    }

    private fun permissionsCheck(): P2pError.MissingPermission? {
        val permissionsMissing = mutableListOf<String>()
        val locationPermission = locationPerms(ctx).also {
            if (!it) { permissionsMissing.add("Missing Location Permission") }
        }
        val nearbyDevicePermission = nearbyDevicePerms(ctx).also {
            if (!it) { permissionsMissing.add("Missing Nearby Device Permission") }
        }
        return when {
            locationPermission && nearbyDevicePermission -> null
            else -> P2pError.MissingPermission(permissionsMissing.joinToString(" "))
        }
    }
}
