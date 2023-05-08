package io.silv.wifi_direct

import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo

sealed class WifiP2pEvent {

    data class StateChanged(val enabled: Boolean): WifiP2pEvent()

    data class PeersChanged(val peers: List<WifiP2pDevice>): WifiP2pEvent()

    data class ConnectionChanged(val networkInfo: NetworkInfo, val p2pInfo: WifiP2pInfo): WifiP2pEvent()

    object ThisDeviceChanged : WifiP2pEvent()

    data class DiscoveryChanged(val started: Boolean): WifiP2pEvent()
}
