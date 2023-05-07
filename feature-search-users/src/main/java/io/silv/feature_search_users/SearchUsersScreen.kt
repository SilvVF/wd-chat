package io.silv.feature_search_users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.silv.shared_ui.utils.collectSideEffect
import io.silv.shared_ui.utils.toast

@Composable
fun SearchUsersScreen(
    viewModel: SearchUsersViewModel = hiltViewModel(),
    p2pDisabled: () -> Unit = {},
    joinedGroup: () -> Unit
) {

    val ctx = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            SearchUsersEvent.WifiP2pDisabled -> p2pDisabled()
            is SearchUsersEvent.ShowToast -> ctx.toast(sideEffect.text)
            is SearchUsersEvent.JoinedGroup -> {
                joinedGroup()
            }
        }
    }

    val users by viewModel.users.collectAsState(emptyList())


    LazyColumn(Modifier.fillMaxSize()) {
        items(users) {user ->
            Text(
                text = user, modifier = Modifier.padding(12.dp).clickable {
                    viewModel.connectToUser(user)
                }
            )
        }
    }
}