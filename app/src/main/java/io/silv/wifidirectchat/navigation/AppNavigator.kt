package io.silv.wifidirectchat.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.silv.wifidirectchat.navigation.destinations.OnboardDestination
import io.silv.wifidirectchat.ui.HomeScreen
import io.silv.wifidirectchat.ui.SplashScreen


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
    ) {
        navigator.navigate(HomeDestination)
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun Splash(
    navigator: DestinationsNavigator,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val onboarded by viewModel.onboarded.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = onboarded) {
        when (onboarded) {
            true -> navigator.navigate(HomeDestination)
            false -> navigator.navigate(OnboardDestination)
            else -> Unit
        }
    }

    SplashScreen()
}

@Destination
@Composable
fun Onboard(
    navigator: DestinationsNavigator,
    mainViewModel: MainActivityViewModel = hiltViewModel()
) {
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
    CreateGroupScreen(
        missingPermission = { navigator.navigate(OnboardDestination) },
        navigateBack = { navigator.navigate(HomeDestination) }
    ) { isGroupOwner, groupOwnerAddress ->
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
    SearchUsersScreen(
        navigateBack = { navigator.navigate(HomeDestination) },
        joinedGroup = { isGroupOwner, groupOwnerAddress ->
            navigator.navigate(ChatDestination(isGroupOwner, groupOwnerAddress))
        },
        missingPermissions = { navigator.navigate(OnboardDestination) }
    )
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