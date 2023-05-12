package io.silv.on_boarding

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.datastore.EncryptedDatastore
import io.silv.image_store.ImageRepository
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val datastore: EncryptedDatastore,
    private val imageRepository: ImageRepository
): EventViewModel<OnboardEvent>() {

    private val lastStep = 2

    private val currentStep = MutableStateFlow(1)
    private val passcode = MutableStateFlow("")

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


    val state = combine(
        currentStep,
        passcode,
    ) { currentStep, passcode ->
        OnboardState(
            currentScreen = currentStep,
            passcode = passcode,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OnboardState())
    private fun navigateToNext(curr: Int) = viewModelScope.launch {
        if (curr == lastStep) {
            eventChannel.send(OnboardEvent.Done)
        }
        currentStep.emit(curr + 1)
    }

    fun onUserPreferencesDone(passcode: String, name: String, uri: Uri) = viewModelScope.launch {
        datastore.writeUserPasscode(passcode)
        datastore.writeUserName(name)
        datastore.writeProfilePictureUri(imageRepository.write(uri))
        navigateToNext(2)
    }

    fun onPermissionsAccepted() = navigateToNext(1)

    fun updateUserPasscode(code: String) = viewModelScope.launch {
        passcode.emit(code)
    }
}

sealed interface OnboardEvent {
    object Done: OnboardEvent
}
