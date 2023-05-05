package io.silv.feature_create_group

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_create_group.use_case.CreateGroupUseCase
import io.silv.feature_create_group.use_case.GroupInfo
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
): EventViewModel<CreateGroupEvent>() {


    fun createGroup() = viewModelScope.launch {
        createGroupUseCase(
            GroupInfo(
                networkName =  "TestNet",
                passPhrase = "password"
            )
        ).fold(
            ifLeft = { err ->
                eventChannel.send(
                    CreateGroupEvent.ShowToast(err.message)
                )
            },
            ifRight = { created ->
                if (created) {
                    eventChannel.send(
                        CreateGroupEvent.GroupCreated
                    )
                }
            }
        )
    }

}

sealed class CreateGroupEvent {
    object GroupCreated: CreateGroupEvent()
    data class ShowToast(val message: String): CreateGroupEvent()
}