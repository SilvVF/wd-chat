package io.silv.shared_ui.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

//interface Events <E> {
//    /**
//     * Don't collect or send events from outside the class extending Events cannot be protected
//     * or private to the limitations within interfaces in Kotlin.
//     * Other options would be to extend ViewModel class and inherit from that.
//     * [issue](https://blog.jetbrains.com/kotlin/2015/09/feedback-request-limitations-on-data-classes/#comment-38249)
//     */
//    val eventChannel: Channel<E>
//        get() = Channel()
//
//    val events: Flow<E>
//        get() = eventChannel.receiveAsFlow()
//}

/**
 * @property eventChannel [Channel] the can accept type E
 * @property events [Flow] receives from channel using [receiveAsFlow] by default
 */
abstract class EventViewModel<E>: ViewModel() {

    protected val eventChannel: Channel<E> = Channel()

    val events: Flow<E>  = eventChannel.receiveAsFlow()
}


/**
 * Observe [EventViewModel.events] in a Compose [LaunchedEffect].
 * @param lifecycleState [Lifecycle.State] in which [state] block runs.
 * [orbit_Impl](https://github.com/orbit-mvi/orbit-mvi/blob/main/orbit-compose/src/main/kotlin/org/orbitmvi/orbit/compose/ContainerHostExtensions.kt)
 */
@SuppressLint("ComposableNaming")
@Composable
fun <SIDE_EFFECT : Any> EventViewModel<SIDE_EFFECT>.collectSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)
) {
    val sideEffectFlow = events
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(sideEffectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sideEffectFlow.collect { sideEffect(it) }
        }
    }
}