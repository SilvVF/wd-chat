package io.silv.wifi_direct

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Looper
import io.silv.wifi_direct.util.locationPerms
import io.silv.wifi_direct.util.nearbyDevicePerms
import kotlinx.coroutines.flow.callbackFlow

class P2pEventsImpl(
    private val ctx: Context,
    private val wifiP2pManager: WifiP2pManager,
):  P2pCallbacks {

    private val channel = wifiP2pManager.initialize(ctx, Looper.getMainLooper()) {
        //cancelled
    }

    @SuppressLint("MissingPermission")
    fun connect(
        wifiP2pConfig: WifiP2pConfig,
        onResult: suspend (started: Boolean) -> Unit
    ) = wifiP2pManager.connect(
            channel,
            wifiP2pConfig, object : ActionListener {
                override fun onSuccess() {
                    suspend { onResult(true) }
                }
                override fun onFailure(p0: Int) {
                    suspend  { onResult(false) }
                }
            }
        )

    @SuppressLint("MissingPermission")
    fun startDiscovery(
        onResult: (started: Boolean) -> Unit
    )  {
        if (locationPerms(ctx) && nearbyDevicePerms(ctx)) {
            wifiP2pManager.discoverPeers(channel, object: ActionListener {
                override fun onSuccess() {
                    onResult(true)
                }
                override fun onFailure(p0: Int) {
                   onResult(false)
                }
            })
        }
    }


}