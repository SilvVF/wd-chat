package io.silv.on_boarding.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.shared_ui.R
import io.silv.shared_ui.components.CodeTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    passcode: String,
    onPasscodeChanged: (passcode: String) -> Unit,
    onDone: (passcode: String, name: String, profilePicture: Uri) -> Unit,
) {

    var name by remember {
        mutableStateOf("")
    }

    var passcodeInfoShowOnce by rememberSaveable {
        mutableStateOf(false)
    }

    var passcodeInfoVisible by rememberSaveable {
        mutableStateOf(false)
    }


    var selectedImageUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        val isImeVisible by keyboardAsState()

        LaunchedEffect(key1 = passcodeInfoVisible) {
            if (passcodeInfoVisible) {
                passcodeInfoShowOnce = true
            }
        }

        if (passcodeInfoVisible) {
            PassCodeInfoPopup {
                passcodeInfoVisible = false
            }
        }

        AnimatedVisibility(visible = !isImeVisible) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(200.dp)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .border(
                            3.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .clip(CircleShape),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    placeholder = painterResource(id = R.drawable.ic_profile_default)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(text = "Upload profile picture")
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "enter a display name") },
                supportingText = {
                                 Text(text = "display name")
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Enter a passcode", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { passcodeInfoVisible = !passcodeInfoVisible }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info for passcode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            CodeTextField(
                text = passcode,
                onValueChanged = { onPasscodeChanged(it) } ,
                maxTextLength = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
                Button(
                    enabled = selectedImageUri != null && name.isNotBlank() && passcode.length == 5,
                    onClick = {
                        onDone(passcode, name, selectedImageUri ?: return@Button)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 42.dp, end = 24.dp)
                ) {
                    Text(text = "Finish profile setup", fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun PassCodeInfoPopup(
    onDismissRequest: () -> Unit,
) {
    Popup(alignment = Alignment.Center, onDismissRequest = onDismissRequest) {
        Column(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(22.dp)
        ) {
            Text(text = "What is the passcode?", style = MaterialTheme.typography.headlineLarge)
            Text(text = "The passcode set here is used when sending chat messages.")
            Text(text = "This will need to match the one set on another device in order to receive the content.")
            Text(text = "The passcode set here can always be changed to match another device.")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("TLDR - ")
                    addStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold),0, "TLDR - ".length )
                    append("match with other device can change")
                }
            )
        }
    }
}


@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}