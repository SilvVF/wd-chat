package io.silv.feature_create_group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import io.silv.shared_ui.utils.collectSideEffect
import io.silv.shared_ui.utils.toast

@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel = hiltViewModel(),
    navigate: () -> Unit
) {

    val ctx = LocalContext.current
    
    viewModel.collectSideEffect { event ->
        when (event) {
            is CreateGroupEvent.GroupCreated -> {  }
            is CreateGroupEvent.ShowToast -> ctx.toast(event.message)
        }
    }

    Column(Modifier.fillMaxSize()) {

        Text("Create Group Screen")
        Button(onClick = { viewModel.createGroup() }) {
            Text(text = "create group")
        }
    }
}