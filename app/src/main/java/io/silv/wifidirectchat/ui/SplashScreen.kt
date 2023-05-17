package io.silv.wifidirectchat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.silv.wifidirectchat.R

@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {

        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_animation_splash_screen))
        val progress by animateLottieCompositionAsState(composition)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            LottieAnimation(
                composition = composition,
                progress = { progress }
            )
        }
    }
}