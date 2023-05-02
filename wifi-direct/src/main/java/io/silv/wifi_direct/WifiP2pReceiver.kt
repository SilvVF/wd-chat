package io.silv.wifi_direct

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.EXTRA_DISCOVERY_STATE
import android.net.wifi.p2p.WifiP2pManager.EXTRA_P2P_DEVICE_LIST
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
import io.silv.wifi_direct.util.logd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WifiP2pReceiver: BroadcastReceiver() {

    private val _eventBroadcast = MutableSharedFlow<WifiP2pEvent>()
    val eventBroadcast = _eventBroadcast.asSharedFlow()

    init {
        logd("created")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            when (intent?.action) {
                WIFI_P2P_STATE_CHANGED_ACTION -> {
                    logd("WIFI_P2P_STATE_CHANGED_ACTION")
                    // Determine if Wi-Fi Direct mode is enabled or not, alert
                    // the Activity.

                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    val isEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    _eventBroadcast.emit(
                        WifiP2pEvent.StateChanged(enabled = isEnabled)
                    )
                }

                WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    logd("WIFI_P2P_PEERS_CHANGED_ACTION")
                    val peers = intent.getParcelableExtra<WifiP2pDeviceList>(EXTRA_P2P_DEVICE_LIST)
                    _eventBroadcast.emit(
                        WifiP2pEvent.PeersChanged(peers = peers?.deviceList?.toList() ?: emptyList())
                    )
                }

                WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    logd("WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    _eventBroadcast.emit(
                        WifiP2pEvent.ConnectionChanged(
                            networkInfo = networkInfo
                        )
                    )
                }

                WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> _eventBroadcast
                    .emit(WifiP2pEvent.ThisDeviceChanged)
                    .also { logd("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION") }
                WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                    logd("WIFI_P2P_DISCOVERY_CHANGED_ACTION")
                    val started = WIFI_P2P_DISCOVERY_STARTED == intent.getIntExtra(EXTRA_DISCOVERY_STATE, -1)
                    _eventBroadcast.emit(WifiP2pEvent.DiscoveryChanged(started = started))
                }
            }
        }
    }
}