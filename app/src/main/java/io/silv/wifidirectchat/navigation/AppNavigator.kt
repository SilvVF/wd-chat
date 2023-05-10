package io.silv.wifidirectchat.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.silv.feature_chat.ChatScreen
import io.silv.feature_create_group.CreateGroupScreen
import io.silv.feature_search_users.SearchUsersScreen
import io.silv.on_boarding.OnboardScreen
import io.silv.wifidirectchat.navigation.destinations.ChatDestination
import io.silv.wifidirectchat.navigation.destinations.CreateGroupDestination
import io.silv.wifidirectchat.navigation.destinations.HomeDestination
import io.silv.wifidirectchat.navigation.destinations.SearchUsersDestination


@Composable
fun AppNavigator() {

    DestinationsNavHost(navGraph = NavGraphs.root)
}


@Destination
@Composable
fun Chat(
    isGroupOwner: Boolean,
    groupOwnerAddress: String,
    navigator: DestinationsNavigator
) {
    ChatScreen(
        isGroupOwner = isGroupOwner,
        groupOwnerAddress = groupOwnerAddress
    )
}

@Destination
@Composable
fun Onboard(
    navigator: DestinationsNavigator
) {
    OnboardScreen(
        viewModel = hiltViewModel(),
    ) {
        navigator.navigate(HomeDestination)
    }
}

@Destination
@Composable
fun CreateGroup(
    navigator: DestinationsNavigator
) {
    CreateGroupScreen { isGroupOwner, groupOwnerAddress ->
        navigator.navigate(
            ChatDestination(isGroupOwner, groupOwnerAddress)
        )
    }
}

@Destination
@Composable
fun SearchUsers(
    navigator: DestinationsNavigator
) {
    SearchUsersScreen { isGroupOwner, groupOwnerAddress ->
        navigator.navigate(
            ChatDestination(isGroupOwner, groupOwnerAddress)
        )
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun Home(
    navigator: DestinationsNavigator
) {

    Column(Modifier.fillMaxSize()) {
        Button(onClick = {
            navigator.navigate(CreateGroupDestination)
        }) {
            Text("Create Group")
        }
        Button(onClick = {
            navigator.navigate(SearchUsersDestination)
        }) {
            Text("Join Group")
        }
    }
}