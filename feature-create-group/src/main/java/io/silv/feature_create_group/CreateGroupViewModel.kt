package io.silv.feature_create_group

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_create_group.use_case.CreateGroupUseCase
import io.silv.feature_create_group.use_case.GroupInfo
import io.silv.feature_create_group.use_case.ObserveWifiDirectEventsUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
): EventViewModel<CreateGroupEvent>() {

    val passcodeLength = 8

    private val passcode = MutableStateFlow("")
    private val passcodeError = passcode.map { code ->
        when {
            code.isBlank() -> PasscodeError.Empty
            code.length < passcodeLength -> PasscodeError.ToShort(passcodeLength)
            else -> null
        }
    }
    private val networkName = MutableStateFlow("")
    private val groupOwner = MutableStateFlow<Boolean?>(null)
    private val groupOwnerAddress = MutableStateFlow<String?>(null)

    val state = combine(
        passcode,
        passcodeError,
        networkName,
        groupOwner,
        groupOwnerAddress
    ) { passcode, passcodeError, networkName, groupOwner, groupOwnerAddress ->
        CreateGroupState(
            passcode = passcode,
            passcodeError = passcodeError,
            networkName = networkName,
            groupOwner = groupOwner,
            groupOwnerAddress = groupOwnerAddress
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CreateGroupState())

    init {
        viewModelScope.launch {
            observeWifiDirectEventsUseCase().collect { event ->
                when(event) {
                    is WifiP2pEvent.ConnectionChanged -> {
                        if (event.p2pInfo.groupFormed) {
                            groupOwner.emit(event.p2pInfo.isGroupOwner)
                            groupOwnerAddress.emit(
                                event.p2pInfo.groupOwnerAddress
                                    .toString()
                                    .replace("/","")
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun changeNetworkName(name: String) =
        viewModelScope.launch { networkName.emit(name) }

    fun changePasscode(code: String) =
        viewModelScope.launch {
            when {
                code.length > passcodeLength -> return@launch
                code.any { !it.isDigit() } -> return@launch
                else -> passcode.emit(code)
            }
        }

    fun createGroup() = viewModelScope.launch {
        createGroupUseCase(
            GroupInfo(
                networkName =  networkName.value,
                passPhrase = passcode.value
            )
        )
            .onLeft { error ->
                when (error) {
                    is P2pError.GenericError ->  eventChannel.send(
                        CreateGroupEvent.ShowToast(error.message)
                    )
                    is P2pError.MissingPermission -> eventChannel.send(
                        CreateGroupEvent.PermissionMissing
                    )
                }
            }
    }

}

data class CreateGroupState(
    val networkName: String = "",
    val passcodeError: PasscodeError? = null,
    val passcode: String = "",
    val groupOwnerAddress: String? = null,
    val groupOwner: Boolean? = null,
)

sealed class PasscodeError(
    @StringRes val stringRes: Int,
    val args: Any = Any()
) {
    object Empty: PasscodeError(R.string.passcode_blank_error)
    data class ToShort(val neededLength: Int): PasscodeError(R.string.passcode_to_short_error, neededLength)
}

sealed class CreateGroupEvent {

    data class ShowToast(val message: String): CreateGroupEvent()

    object PermissionMissing: CreateGroupEvent()
}