package io.silv.on_boarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.silv.on_boarding.R
import io.silv.shared_ui.components.ExpandableExplanation

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    paddingValues: PaddingValues,
    permissions: List<String>,
    allGranted: () -> Unit
) {

    val permissionsState = rememberMultiplePermissionsState(permissions)

    if (permissionsState.allPermissionsGranted) {
        allGranted()
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.lottie_animation_permissions
        )
    )
    val progress by animateLottieCompositionAsState(composition)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(
                start = 12.dp,
                end = 12.dp
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        )
        ExpandableExplanation(
            hint = stringResource(id = R.string.permissions_exp_hint),
            explanation = stringResource(id = R.string.permissions_explanation),
        )
        OutlinedButton(
            onClick = { permissionsState.launchMultiplePermissionRequest() },
        ) {
            Text(
                text = stringResource(id = R.string.permission_btn_text)
            )
        }
    }
}