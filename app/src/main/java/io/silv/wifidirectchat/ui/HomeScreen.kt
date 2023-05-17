package io.silv.wifidirectchat.ui

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    val profilePicture by viewModel.profilePictureFlow.collectAsStateWithLifecycle(initialValue = Uri.EMPTY)
    val username by viewModel.usernameFlow.collectAsStateWithLifecycle(initialValue = "")
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(
                        (screenHeight * 0.3f).dp
                    )
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePicture)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(
                    id = io.silv.on_boarding.R.string.profile_pic
                ),
            )
            Text(text = username)
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