package io.silv.feature_search_users

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.silv.shared_ui.components.PagerCodeAndNetworkName
import io.silv.shared_ui.utils.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class
)
@Composable
fun SearchUsersScreen(
    viewModel: SearchUsersViewModel = hiltViewModel(),
    missingPermissions: () -> Unit,
    navigateBack: () -> Unit,
    joinedGroup: (isGroupOwner: Boolean, groupOwnerAddress: String) -> Unit
) {

    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            SearchUsersEvent.WifiP2pDisabled -> snackbarHostState.showSnackbar(
                "Wifi Direct has been disabled enable in device settings",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            is SearchUsersEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                sideEffect.text, withDismissAction = true, duration = SnackbarDuration.Short
            )
            is SearchUsersEvent.JoinedGroup -> {
                joinedGroup(sideEffect.isGroupOwner, sideEffect.groupOwnerAddress)
            }
            SearchUsersEvent.MissingPermissions -> missingPermissions()
        }
    }

    val users by viewModel.users.collectAsState(emptyList())
    val keyboard = LocalSoftwareKeyboardController?.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navigateBack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        val refreshing by viewModel.refreshing.collectAsState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = { viewModel.onPullToRefresh() }
        )

        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.connect_hint)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = users,
                        key = {user -> user.name + user.mac }
                    ) {user ->

                        val nameText by remember(user.name) {
                            derivedStateOf {
                                "device name: ${user.name}"
                            }
                        }

                        val macText by remember(user.mac) {
                            derivedStateOf {
                                "device address: ${user.mac}"
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding(22.dp)
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onPrimaryContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.connectToUser(user.name) },
                        ) {
                            Text(
                                text = nameText,
                                modifier = Modifier
                                    .padding(12.dp)
                            )
                            Text(
                                text = macText,
                                modifier = Modifier
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}