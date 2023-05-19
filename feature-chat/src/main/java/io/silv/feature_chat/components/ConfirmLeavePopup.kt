package io.silv.feature_chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.silv.feature_chat.R

@Composable
fun ConfirmLeavePopup(
    visible: Boolean,
    isGroupOwner: Boolean,
    onDismiss: () -> Unit,
    navigateBack: () -> Unit
) {
    if (visible) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(22.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(
                        id = if(isGroupOwner) {
                            R.string.confirm_leave_text_owner
                        } else {
                            R.string.confirm_leave_text
                        }
                    )
                )
                OutlinedButton(
                    onClick = {
                        navigateBack()
                    }
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.disconnect_btn_text
                        )
                    )
                }
            }
        }
    }
}