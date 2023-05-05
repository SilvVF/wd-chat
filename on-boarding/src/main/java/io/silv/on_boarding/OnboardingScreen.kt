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
    permissionsToRequest: List<String>,
    onDone: () -> Unit
) {

    viewModel.collectSideEffect {
        when(it) {
            OnboardEvent.Done -> onDone()
        }
    }

    val state by viewModel.state.collectAsState()

    when(state.currentScreen) {
      1 -> PermissionsScreen(permissions = permissionsToRequest) {
          viewModel.onPermissionsAccepted()
      }
      2 -> UserPreferencesScreen(
          passcode = state.passcode,
          onPasscodeChanged = { passcode ->
              viewModel.updateUserPasscode(passcode = passcode)
          }
      ) { passcode, name ->
          viewModel.onUserPreferencesDone(passcode, name)
      }
    }
}


