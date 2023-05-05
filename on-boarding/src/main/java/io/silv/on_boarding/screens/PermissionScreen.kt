package io.silv.on_boarding.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

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