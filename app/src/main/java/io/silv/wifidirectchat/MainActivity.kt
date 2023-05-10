package io.silv.wifidirectchat

import android.Manifest
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import io.silv.feature_chat.ChatScreen
import io.silv.feature_create_group.CreateGroupScreen
import io.silv.feature_search_users.SearchUsersScreen
import io.silv.shared_ui.theme.WifiDirectChatTheme
import io.silv.on_boarding.OnboardScreen
import io.silv.wifi_direct.WifiP2pReceiver
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

    private val permissions = buildList {
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.CHANGE_NETWORK_STATE)
        add(Manifest.permission.INTERNET)
        add(Manifest.permission.ACCESS_NETWORK_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WifiDirectChatTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                     composable("start") {
                         Surface(
                             modifier = Modifier.fillMaxSize(),
                             color = MaterialTheme.colorScheme.background
                         ) {
                             OnboardScreen(
                                 viewModel = hiltViewModel(),
                                 permissionsToRequest = permissions
                             ) {
                                navController.navigate("next")
                             }
                         }
                     }
                    composable("home") {
                        Column(Modifier.fillMaxSize()) {
                            Button(onClick = {
                                navController.navigate("create-group")
                            }) {
                                Text("Create Group")
                            }
                            Button(onClick = {
                                navController.navigate("next")
                            }) {
                                Text("Join Group")
                            }
                        }
                    }
                    composable("next") {
                        SearchUsersScreen { isGroupOwner, groupOwnerAddress ->
                            navController.navigate(
                                "chat/$isGroupOwner/${groupOwnerAddress}",

                            )
                        }
                    }
                    composable("create-group") {
                        CreateGroupScreen {
                            navController.navigate("chat")
                        }
                    }
                    composable(
                        "chat/{owner}/{address}",
                        arguments = listOf(
                            navArgument("owner") { NavType.StringType },
                            navArgument("address") { NavType.StringType }
                        )
                    ) { backStackEntry ->
                        Log.d("P2P", "djfhkashdfjshdfjrsjkdfjkasdfhjkashdfrjs")
                        ChatScreen(
                            backStackEntry.arguments?.getString("owner")?.first() == 't',
                            backStackEntry.arguments?.getString("address") ?: "127.0.0.1"
                        )
                    }
                }
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
