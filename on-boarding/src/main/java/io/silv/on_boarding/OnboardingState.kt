package io.silv.on_boarding

import androidx.annotation.StringRes

data class OnboardState(
    val currentScreen: Int = 0,
    val name: String = "",
    val passcode: String = "",
    val error: OnboardError = OnboardError()
) {

    data class OnboardError(
        @StringRes val nameError: Int? = null,
        @StringRes val passcodeError: Int? = null,
        @StringRes val permissionError: Int? = null,
    )
}