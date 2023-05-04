package io.silv.shared_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CodeTextField(
    text: String,
    onValueChanged: (String) -> Unit,
    maxTextLength: Int,
    modifier: Modifier = Modifier
) {

    BasicTextField(
        value = text,
        onValueChange = {
            if (it.length <= maxTextLength) {
                onValueChanged(it)
            }
        },
        decorationBox = { innerTextField ->
            Row {
                text.forEachIndexed { idx, char ->
                    CodeCharBox(char = char, idx == text.lastIndex)
                }
                for (i in 0 until maxTextLength - text.length) {
                    EmptyCharBox()
                }
                DeleteKey {
                    if (text.isNotEmpty()) {
                        onValueChanged(text.dropLast(1))
                    }
                }
            }
        }
    )
}

@Composable
private fun DeleteKey(
    delete: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp),
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
            .size(40.dp)
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
            .size(40.dp)
            .border(width = 2.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString()
        )
    }
}

