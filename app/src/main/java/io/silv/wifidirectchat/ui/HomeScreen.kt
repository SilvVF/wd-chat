package io.silv.wifidirectchat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.spec.Direction
import io.silv.shared_ui.components.ExpandableExplanation
import io.silv.wifidirectchat.MainActivityViewModel
import io.silv.wifidirectchat.R
import io.silv.wifidirectchat.navigation.destinations.CreateGroupDestination
import io.silv.wifidirectchat.navigation.destinations.SearchUsersDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainActivityViewModel = hiltViewModel(),
    navigate: (Direction) -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ExpandableExplanation(
                hint = stringResource(id = R.string.create_group_hint),
                explanation = stringResource(id = R.string.create_group_explanation)
            )
            OutlinedButton(
                onClick = { navigate(CreateGroupDestination) }
            ) {
                Text(
                    text = stringResource(id = R.string.create_group_btn)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            ExpandableExplanation(
                hint = stringResource(id = R.string.search_users_hint),
                explanation = stringResource(id = R.string.search_users_explantion)
            )
            OutlinedButton(
                onClick = { navigate(SearchUsersDestination) }
            ) {
                Text(
                    text = stringResource(id = R.string.search_users_btn)
                )
            }
        }
    }
}