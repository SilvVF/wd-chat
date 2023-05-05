package io.silv.wifi_direct

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.callbackFlow

interface P2pCallbacks {

    val peerListListenerCallbackFlow get() = callbackFlow<List<WifiP2pDevice>> {
        WifiP2pManager.PeerListListener { devices ->
            trySend(devices.deviceList.toList())
        }
        awaitCancellation()
    }

    val groupInfoListenerCallbackFlow get() = callbackFlow<WifiP2pGroup> {
        WifiP2pManager.GroupInfoListener { groupInfo ->
            trySend(groupInfo)
        }
        awaitCancellation()
    }

    val p2pStateListenerCallbackFlow get() = callbackFlow<Boolean> {
        WifiP2pManager.P2pStateListener {
            trySend(
                when (it) {
                    WIFI_P2P_STATE_ENABLED -> true
                    else -> false
                }
            )
        }
        awaitCancellation()
    }
}