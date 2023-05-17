package io.silv.on_boarding.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.on_boarding.R

@Composable
fun ProfilePictureScreen(
    paddingValues: PaddingValues,
    onDone: (profilePicture: Uri) -> Unit
) {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Box(
            modifier = Modifier.weight(1f, false)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(
                        (screenHeight * 0.4f).dp
                    )
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedImageUri)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(id = R.string.profile_pic),
            )
            if (selectedImageUri == null) {
                Text(
                    text = stringResource(id = R.string.profile_pic_hint),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = {
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            Text(
                text = stringResource(id = R.string.profile_pic_upload_btn)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            enabled = selectedImageUri != null,
            onClick = {
                selectedImageUri?.let {
                    onDone(it)
                }
            },
            modifier = Modifier.imePadding()
        ) {
            Text(
                text = stringResource(id = R.string.finish_profile_picture),
                fontSize = 24.sp
            )
        }
    }
}