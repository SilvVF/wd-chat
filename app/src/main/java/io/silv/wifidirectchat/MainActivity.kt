package io.silv.wifidirectchat

import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.silv.shared_ui.theme.WifiDirectChatTheme
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifidirectchat.navigation.AppNavigator
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

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
}
