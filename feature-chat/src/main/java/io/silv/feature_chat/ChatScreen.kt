package io.silv.feature_chat

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ChatScreen(
  viewModel: ChatViewModel = hiltViewModel()
) {

    val group = viewModel.group

    Column(Modifier.fillMaxSize()) {
        Text("Chat Screen")

              Text(group?.networkName + "networkName")
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Text(group?.networkId.toString() + "networkID")
              }
              Text(group?.`interface`.toString())
              Text(group?.passphrase.toString())
              Text(group?.clientList.toString())
              Text(group?.isGroupOwner.toString())
              Text(group?.owner.toString())

    }
}