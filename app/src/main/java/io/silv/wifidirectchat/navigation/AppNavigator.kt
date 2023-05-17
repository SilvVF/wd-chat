package io.silv.wifidirectchat.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.silv.feature_chat.ChatScreen
import io.silv.feature_create_group.CreateGroupScreen
import io.silv.feature_search_users.SearchUsersScreen
import io.silv.on_boarding.OnboardScreen
import io.silv.wifidirectchat.MainActivityViewModel
import io.silv.wifidirectchat.navigation.destinations.ChatDestination
import io.silv.wifidirectchat.navigation.destinations.HomeDestination
import io.silv.wifidirectchat.ui.HomeScreen


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

@RootNavGraph(start = true)
@Destination
@Composable
fun Onboard(
    navigator: DestinationsNavigator,
    mainViewModel: MainActivityViewModel = hiltViewModel()
) {

    val onboarded by mainViewModel.onboarded.collectAsStateWithLifecycle()

    if (onboarded) {
        navigator.navigate(HomeDestination)
    }

    OnboardScreen(
        viewModel = hiltViewModel(),
    ) {
        mainViewModel.onboardComplete()
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


@Destination
@Composable
fun Home(
    navigator: DestinationsNavigator
) {
    HomeScreen { direction ->
        navigator.navigate(direction)
    }
}