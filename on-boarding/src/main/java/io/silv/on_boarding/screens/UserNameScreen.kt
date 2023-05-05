package io.silv.on_boarding.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.silv.shared_ui.components.CodeTextField

@Composable
fun UserPreferencesScreen(
    passcode: String,
    onPasscodeChanged: (passcode: String) -> Unit,
    onDone: (passcode: String, name: String) -> Unit,
) {

    var text by remember {
        mutableStateOf("df")
    }



    CodeTextField(
        text = passcode,
        onValueChanged = { text = it } ,
        maxTextLength = 6
    )
}