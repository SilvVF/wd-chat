package io.silv.on_boarding

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(): EventViewModel<OnboardEvent>() {

    private val lastStep = 2

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    fun navigateToNext(curr: Int) = viewModelScope.launch {
        if (curr == lastStep) {
            eventChannel.send(OnboardEvent.Done)
        }
        _currentStep.emit(curr + 1)
    }
}

sealed interface OnboardEvent {
    object Done: OnboardEvent
}
