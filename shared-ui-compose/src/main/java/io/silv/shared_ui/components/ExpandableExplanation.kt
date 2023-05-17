package io.silv.shared_ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableExplanation(
    modifier: Modifier = Modifier,
    hint: String,
    explanation: String,
) {

    var showExplanation by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = hint,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ) {
                    showExplanation = !showExplanation
                }
            )
            IconButton(
                onClick = {
                    showExplanation = !showExplanation
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = hint,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        AnimatedVisibility(
            visible = showExplanation,
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            )
        ) {
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}