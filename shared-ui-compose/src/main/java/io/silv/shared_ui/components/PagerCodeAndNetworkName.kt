package io.silv.shared_ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PagerCodeAndNetworkName(
    code: String,
    codeHint: String,
    codeExplanation: String,
    networkName: String,
    networkNameHint: String,
    networkNameExplanation: String,
    onCodeValueChange: (String) -> Unit,
    onNetworkNameValueChange: (String) -> Unit,
) {
    val pagerState = rememberPagerState()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequesterCode = remember { FocusRequester() }
    val focusRequesterName = remember { FocusRequester() }

    var anyTextFieldsFocused by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(pagerState.settledPage) {
        if (!anyTextFieldsFocused) { return@LaunchedEffect }
        when (pagerState.settledPage) {
            0 -> focusRequesterCode.requestFocus()
            1 -> focusRequesterName.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(2) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(2.dp, end = 8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(18.dp)
            )
        }
    }
    HorizontalPager(
        pageCount = 2,
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        Box(
            Modifier
                .graphicsLayer {
                    // Calculate the absolute offset for the current page from the
                    // scroll position. We use the absolute value which allows us to mirror
                    // any effects for both directions
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    fun lerp(a: Float, b: Float, f: Float): Float {
                        return (a * (1f - f)) + (b * f);
                    }
                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        a = 0.5f,
                        b = 1f,
                        f = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
        ) {
            when(page) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.requiredHeightIn(min = 200.dp)
                    ) {
                        ExpandableExplanation(
                            hint = codeHint,
                            explanation = codeExplanation
                        )
                        CodeTextField(
                            text = code,
                            modifier = Modifier
                                .focusRequester(focusRequesterCode)
                                .onFocusChanged {
                                    anyTextFieldsFocused = anyTextFieldsFocused || it.hasFocus
                                },
                            onValueChanged = onCodeValueChange,
                            maxTextLength = 8,
                            rows = 2
                        )
                    }
                }
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.requiredHeightIn(min = 200.dp)
                    ) {
                        ExpandableExplanation(
                            hint = networkNameHint,
                            explanation = networkNameExplanation,
                        )
                        OutlinedTextField(
                            value = networkName,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .focusRequester(focusRequesterName)
                                .onFocusChanged {
                                       anyTextFieldsFocused = anyTextFieldsFocused || it.hasFocus
                                },
                            singleLine = true,
                            onValueChange = onNetworkNameValueChange,
                            supportingText = {
                                Text(
                                    text = "Network Name"
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "enter a network name"
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}