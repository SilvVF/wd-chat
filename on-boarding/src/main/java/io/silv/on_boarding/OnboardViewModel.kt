package io.silv.on_boarding

import android.Manifest
import android.os.Build
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.datastore.EncryptedDatastore
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val datastore: EncryptedDatastore
): EventViewModel<OnboardEvent>() {

    private val lastStep = 2

    private val currentStep = MutableStateFlow(1)
    private val passcode = MutableStateFlow("")
    private val passcodeError = MutableStateFlow(false)
    private val nameError = MutableStateFlow(false)
    private val permissionError = MutableStateFlow(false)

    val permissions = buildList {
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.CHANGE_NETWORK_STATE)
        add(Manifest.permission.INTERNET)
        add(Manifest.permission.ACCESS_NETWORK_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    private val errors = combine(
        passcodeError,
        nameError,
        permissionError
    ) { passErr, nameErr, permsErr ->
        OnboardState.OnboardError(
            nameError = if (nameErr) 1 else null,
            passcodeError = if (passErr) 1 else null,
            permissionError = if (permsErr) 1 else null
        )
    }


    val state = combine(
        currentStep,
        passcode,
        errors
    ) { currentStep, passcode, errors ->
        OnboardState(
            currentScreen = currentStep,
            passcode = passcode,
            error = errors
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OnboardState())
    private fun navigateToNext(curr: Int) = viewModelScope.launch {
        if (curr == lastStep) {
            eventChannel.send(OnboardEvent.Done)
        }
        currentStep.emit(curr + 1)
    }

    fun onUserPreferencesDone(passcode: String, name: String) = viewModelScope.launch {
        datastore.writeUserPasscode(passcode)
        datastore.writeUserName(name)
        val passMatches = (datastore.readUserPasscode().first() != passcode)
            .also { passcodeError.emit(it) }
        val nameMatches = (datastore.readUserName().first() != name)
            .also { nameError.emit(it) }
        if(listOf(passMatches, nameMatches)
                .all { match -> match }
        ) {
            navigateToNext(2)
        }
    }

    fun onPermissionsAccepted() = navigateToNext(1)
    fun updateUserPasscode(passcode: String) = viewModelScope.launch {
        this@OnboardViewModel.passcode.emit(passcode)
    }

    fun updateUserName(name: String) = viewModelScope.launch {
        datastore.writeUserName(name)
    }
}

sealed interface OnboardEvent {
    object Done: OnboardEvent
}
