package io.silv.wifi_direct

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * [BroadcastReceiver] for WifiP2p events.
 * @property p2pBroadcast  [WifiP2pEvent] Emitted from [Flow].
 * Collects events for following intent filters
 * - [WIFI_P2P_STATE_CHANGED_ACTION]
 * - [WIFI_P2P_PEERS_CHANGED_ACTION]
 * - [WIFI_P2P_CONNECTION_CHANGED_ACTION]
 * - [WIFI_P2P_THIS_DEVICE_CHANGED_ACTION],
 * - [WIFI_P2P_DISCOVERY_CHANGED_ACTION]
 */

class WifiP2pReceiver(
    context: Context
) {
    private val intentFilter = IntentFilter().apply {
        addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        addAction(WIFI_P2P_DISCOVERY_CHANGED_ACTION)
    }

    val p2pBroadcast: Flow<WifiP2pEvent> = context.flowBroadcasts(intentFilter)
        .map { intent ->
            when (intent.action) {
                WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    val isEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    WifiP2pEvent.StateChanged(enabled = isEnabled)

                }
                WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    val peers = intent.getParcelableExtra<WifiP2pDeviceList>(EXTRA_P2P_DEVICE_LIST)
                    WifiP2pEvent.PeersChanged(peers = peers?.deviceList?.toList() ?: emptyList())
                }
                WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    val p2pInfo: WifiP2pInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                    WifiP2pEvent.ConnectionChanged(
                        networkInfo = networkInfo,
                        p2pInfo = p2pInfo ?: return@map WifiP2pEvent.Unknown
                    )
                }
                WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    logd("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                    WifiP2pEvent.ThisDeviceChanged
                }
                WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                    logd("WIFI_P2P_DISCOVERY_CHANGED_ACTION")
                    val started = WIFI_P2P_DISCOVERY_STARTED == intent.getIntExtra(EXTRA_DISCOVERY_STATE, -1)
                    WifiP2pEvent.DiscoveryChanged(started = started)
                }
                else -> WifiP2pEvent.Unknown
            }
        }
}

private fun Context.flowBroadcasts(intentFilter: IntentFilter): Flow<Intent> {
    val resultChannel = MutableStateFlow(Intent())

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            resultChannel.value = intent
        }
    }

    return resultChannel.onStart { registerReceiver(receiver, intentFilter) }
        .onCompletion { unregisterReceiver(receiver) }
}