package io.silv.on_boarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.silv.shared_ui.components.CodeTextField
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

    when(viewModel.currentStep.collectAsState().value) {
      1 -> PermissionsScreen(permissions = permissionsToRequest) {
          viewModel.navigateToNext(1)
      }
      else -> SetName {

      }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    permissions: List<String>,
    allGranted: () -> Unit
) {

    val permissionsState = rememberMultiplePermissionsState(permissions)

    if (permissionsState.allPermissionsGranted) {
        allGranted()
    }


    Column(Modifier.fillMaxSize()) {
        Text("reasons why persmissions are needed")
        OutlinedButton(
            onClick = { permissionsState.launchMultiplePermissionRequest() },
        ) {
            Text("accept permissions")
        }
    }
}

@Composable
fun SetName(
    onDone: () -> Unit
) {

    var text by remember {
        mutableStateOf("df")
    }

    CodeTextField(text = text, onValueChanged = { text = it } , maxTextLength = 6)
}