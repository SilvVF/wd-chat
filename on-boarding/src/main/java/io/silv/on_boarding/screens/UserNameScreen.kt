package io.silv.on_boarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.silv.on_boarding.NameError
import io.silv.on_boarding.R
import io.silv.shared_ui.components.ExpandableExplanation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNameScreen(
    paddingValues: PaddingValues,
    name: String,
    onNameChanged: (String) -> Unit,
    errors: List<NameError>,
    onDone: (name: String) -> Unit,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.lottie_animation_username)
    )
    val progress by animateLottieCompositionAsState(composition)
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .size(
                    (screenHeight * 0.4f).dp
                )
                .weight(1f, false)
        )
        ExpandableExplanation(
            hint = stringResource(id = R.string.username_explanation_hint),
            explanation = stringResource(id = R.string.reason_for_username)
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.username_placeholder)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = errors.isNotEmpty(),
            keyboardActions = KeyboardActions(
                onNext = {
                    onDone(name)
                }
            ),
            supportingText = {
                Text(
                    text = stringResource(id = R.string.username_supporting_text)
                )
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        errors.forEach { err ->
            Text(
                text = when(err) {
                    NameError.Blank -> stringResource(id = R.string.name_error_blank)
                },
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(
            modifier = Modifier.height(32.dp)
        )
        Button(
            enabled = name.isNotBlank(),
            onClick = {
                onDone(name)
            },
            modifier = Modifier
                .imePadding()
        ) {
            Text(
                text = stringResource(id = R.string.finish_username_setup),
                fontSize = 24.sp
            )
        }
    }
}
