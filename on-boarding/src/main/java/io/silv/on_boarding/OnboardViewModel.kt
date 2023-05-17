package io.silv.on_boarding

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.datastore.EncryptedDatastore
import io.silv.image_store.ImageRepository
import io.silv.on_boarding.use_case.CheckPermissionsGrantedUseCase
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userStore: EncryptedDatastore,
    private val imageRepository: ImageRepository,
    private val checkPermissionsGrantedUseCase: CheckPermissionsGrantedUseCase
): EventViewModel<OnboardEvent>() {

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
    private val currentStepKey = "CURRENT_STEP"
    private val currentStep = MutableStateFlow(
        savedStateHandle[currentStepKey] ?: getFirstStep()
    )
    private val mutableUsername = MutableStateFlow("")
    private val nameErrors = mutableUsername.map {
        buildList {
            if (it.isBlank()) {
                add(NameError.Blank)
            }
        }
    }
    private val loading = MutableStateFlow(true)

    val state = combine(
        currentStep,
        mutableUsername,
        nameErrors,
        loading,
    ) { currentStep, name, nameErrors, _ ->
        savedStateHandle[currentStepKey] = currentStep
        OnboardState(
            loading = false,
            step = currentStep,
            name =  name,
            nameErrors = nameErrors
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OnboardState())


    private fun getFirstStep(): OnboardStep {
        val allGranted = checkPermissionsGrantedUseCase(permissions)
        return when {
            allGranted -> OnboardStep.Permissions.next()
            else -> OnboardStep.Permissions
        }
    }

    fun navigateBack() = viewModelScope.launch {
        if (currentStep.value.position > 2) {
            currentStep.emit(
                currentStep.value.prev()
            )
        }
    }

    private fun navigateToNext(curr: OnboardStep) = viewModelScope.launch {
       currentStep.emit(
           curr.next()
       )
    }

    fun onProfilePictureDone(uri: Uri) = viewModelScope.launch {
        runCatching {
            imageRepository.write(uri)
        }
            .onSuccess {
                navigateToNext(OnboardStep.ProfilePicture)
            }
            .onFailure {
                eventChannel.send(
                    OnboardEvent.ShowSnackBar(
                        it.message ?: "Unable To upload picture"
                    )
                )
            }
    }

    fun onUsernameDone(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) {
            userStore.writeUserName(name)
            navigateToNext(OnboardStep.Username)
        }
    }


    fun handleNameChange(name: String) =
        viewModelScope.launch { mutableUsername.emit(name) }

    fun onPermissionsAccepted() = navigateToNext(OnboardStep.Permissions)
}

enum class OnboardStep(val position: Int) {
    Permissions(1),
    Username(2),
    ProfilePicture(3),
    Done(4)
}


fun OnboardStep.prev(): OnboardStep {
    return OnboardStep
        .values()
        .find { it.position == this.position - 1 }
        ?: run {
            OnboardStep.values().first()
        }
}

fun OnboardStep.next(): OnboardStep {
    return OnboardStep
        .values()
        .find { it.position == this.position + 1 }
        ?: OnboardStep.Done
}

sealed interface NameError {
    object Blank: NameError
}


sealed interface OnboardEvent {
    data class ShowSnackBar(val message: String): OnboardEvent
}
