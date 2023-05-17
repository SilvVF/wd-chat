package io.silv.on_boarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.silv.on_boarding.screens.PermissionsScreen
import io.silv.on_boarding.screens.ProfilePictureScreen
import io.silv.on_boarding.screens.UserNameScreen
import io.silv.shared_ui.utils.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardScreen(
    viewModel: OnboardViewModel,
    onDone: () -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsState()


    viewModel.collectSideEffect {
        when(it) {
            is OnboardEvent.ShowSnackBar ->
                snackbarHostState.showSnackbar(
                    message = it.message,
                    duration = SnackbarDuration.Short
                )
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

    if (state.step == OnboardStep.Done) {
        onDone()
    }

     Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHost = {
            snackbarHostState.currentSnackbarData?.let { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                id = when(state.step) {
                                    OnboardStep.Permissions -> R.string.permissions_title
                                    OnboardStep.Username -> R.string.username_title
                                    OnboardStep.ProfilePicture -> R.string.profile_picture_title
                                    OnboardStep.Done -> R.string.done_title
                                }
                            ),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            text = stringResource(
                                id = R.string.current_step,
                                state.step.position,
                                OnboardStep.values().last().position - 1
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth(0.25f)
                        )
                    }
                },
                 navigationIcon = {
                     if (state.step.position > 2) {
                         IconButton(
                             onClick = {
                                 viewModel.navigateBack()
                             }
                         ) {
                             Icon(
                                 imageVector = Icons.Default.ArrowBack,
                                 contentDescription = "back"
                             )
                         }
                     }
                 }
             )
         }
    ) { padding ->
         if (state.loading) {
            /*waiting to see if start will be at the permissions screen
              on uninstall and reinstall permissions may be granted and this will stop
              ui jank on instant navigation */
             return@Scaffold
         }
         AnimatedContent(
             targetState = state.step
         ) {targetState ->
             when(targetState) {
                 OnboardStep.Permissions -> PermissionsScreen(
                     paddingValues = padding,
                     permissions = viewModel.permissions,
                     allGranted = { viewModel.onPermissionsAccepted() }
                 )
                 OnboardStep.Username ->
                     UserNameScreen(
                         paddingValues = padding,
                         name = state.name,
                         errors = state.nameErrors,
                         onNameChanged = {
                             viewModel.handleNameChange(it)
                         },
                         onDone = { name ->
                             viewModel.onUsernameDone(name)
                         }
                     )
                 OnboardStep.ProfilePicture ->
                     ProfilePictureScreen(
                         paddingValues = padding,
                         onDone = { profilePicture ->
                             viewModel.onProfilePictureDone(profilePicture)
                         }
                     )
                 else -> Unit
             }
         }
    }
}


