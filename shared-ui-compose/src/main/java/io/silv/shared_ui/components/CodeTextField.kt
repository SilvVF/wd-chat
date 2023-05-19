package io.silv.shared_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CodeTextField(
    text: String,
    onValueChanged: (String) -> Unit,
    maxTextLength: Int,
    rows: Int,
    modifier: Modifier = Modifier
) {
    val imeController = LocalSoftwareKeyboardController.current
    val rowSize = remember(rows, maxTextLength) {
        (maxTextLength / rows).coerceAtLeast(1)
    }

    val chunkedList by remember(text) {
        derivedStateOf {
           List(maxTextLength){ text.getOrNull(it) }.chunked(rowSize)
        }
    }

    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = {
            if (it.length <= maxTextLength) {
                onValueChanged(it)
            }
        },
        decorationBox = { innerTextField ->
            Column {
                chunkedList.forEachIndexed { chunkIdx, chunk ->
                    Row {
                        chunk.forEachIndexed { index, c ->
                            val idx = chunkIdx * index
                            if (c != null) {
                                CodeCharBox(char = c, focused = idx == text.lastIndex)
                            } else {
                                EmptyCharBox()
                            }
                        }
                    }
                }
                DeleteKey(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(50.dp)
                        .align(Alignment.End),
                ) {
                    if (text.isNotEmpty()) {
                        onValueChanged(text.dropLast(1))
                    }
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { imeController?.hide() }
        )
    )
}

@Composable
private fun DeleteKey(
    modifier: Modifier = Modifier,
    delete: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = { delete() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "delete key",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun EmptyCharBox() {

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(shape)
            .size(50.dp)
            .border(width = 2.dp, MaterialTheme.colorScheme.onBackground, shape)
            .background(Color.LightGray),
    ) {

    }
}

@Composable
private fun CodeCharBox(
    char: Char,
    focused: Boolean,
) {

    val borderColor by animateColorAsState(
        targetValue = if (focused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        label = "CodeCharacterBoxOutline"
    )

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(shape)
            .size(50.dp)
            .border(width = 2.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString()
        )
    }
}

