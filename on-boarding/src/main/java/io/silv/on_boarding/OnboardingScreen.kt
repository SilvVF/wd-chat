package io.silv.on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.silv.on_boarding.screens.PermissionsScreen
import io.silv.on_boarding.screens.UserPreferencesScreen
import io.silv.shared_ui.utils.collectSideEffect

@Composable
fun OnboardScreen(
    viewModel: OnboardViewModel,
    onDone: () -> Unit
) {

    viewModel.collectSideEffect {
        when(it) {
            OnboardEvent.Done -> onDone()
        }
    }

    val state by viewModel.state.collectAsState()

    when(state.currentScreen) {
          1 -> PermissionsScreen(permissions = viewModel.permissions) {
              viewModel.onPermissionsAccepted()
          }
          2 -> UserPreferencesScreen(
              passcode = state.passcode,
              onPasscodeChanged = { passcode ->
                  viewModel.updateUserPasscode(passcode)
              }
          ) { passcode, name, profilePicture ->
              viewModel.onUserPreferencesDone(passcode, name, profilePicture)
          }
    }
}


