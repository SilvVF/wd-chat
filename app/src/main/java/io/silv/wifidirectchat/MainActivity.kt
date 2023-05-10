package io.silv.wifidirectchat

import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.silv.shared_ui.theme.WifiDirectChatTheme
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifidirectchat.navigation.AppNavigator
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var receiver: WifiP2pReceiver

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val viewModel: MainActivityViewModel = hiltViewModel()

            LaunchedEffect(key1 = true) {
                viewModel.collectWifiEvents()
            }

            WifiDirectChatTheme {
                AppNavigator()
            }
        }
    }
    /* register the broadcast receiver with the intent values to be matched */
    override fun onResume() {
        super.onResume()
        receiver.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        receiver.also { receiver ->
            unregisterReceiver(receiver)
        }
    }
}
