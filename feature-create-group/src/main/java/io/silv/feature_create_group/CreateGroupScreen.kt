package io.silv.feature_create_group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.silv.shared_ui.components.CodeTextField
import io.silv.shared_ui.utils.collectSideEffect
import io.silv.shared_ui.utils.toast
import io.silv.wifi_direct.types.P2pError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel = hiltViewModel(),
    missingPermission: () -> Unit,
    navigate: (isGroupOwner: Boolean, groupOwnerAddress: String) -> Unit,
) {

    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    viewModel.collectSideEffect { event ->
        when (event) {
            is CreateGroupEvent.ShowToast -> {
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Long,
                )
            }
            is CreateGroupEvent.PermissionMissing -> missingPermission()
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.groupOwner, state.groupOwnerAddress) {
        val groupOwner = state.groupOwner
        val address = state.groupOwnerAddress
        if (groupOwner != null && address != null) {
            navigate(groupOwner, address)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.passcode_hint)
                )
                Spacer(modifier = Modifier.height(22.dp))
                CodeTextField(
                    text = state.passcode,
                    rows = 2,
                    onValueChanged = { viewModel.changePasscode(it) },
                    maxTextLength = viewModel.passcodeLength
                )
                Spacer(modifier = Modifier.height(32.dp))
                state.passcodeError?.let {
                    Text(
                        text = stringResource(id = it.stringRes, it.args),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedTextField(
                value = state.networkName,
                onValueChange = {
                    viewModel.changeNetworkName(it)
                },
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.network_name)
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.network_name_hint)
                    )
                }
            )
            OutlinedButton(
                onClick = { viewModel.createGroup() },
                enabled = state.passcodeError == null,
                modifier = Modifier.imePadding()
            ) {
                Text(
                    text = stringResource(id = R.string.create_group_btn_label),
                    fontSize = 24.sp
                )
            }
        }
    }
}