package io.silv.shared_ui.components

import android.content.res.Resources
import android.net.Uri
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.ViewCompat
import androidx.core.widget.addTextChangedListener

@Composable
fun RichContentEditText(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    editTextBlock: EditText.() -> Unit = {},
    onReceivedContent:  (Uri) -> Unit = {}
) {


    val listener = remember {
        OnReceiveContentListener { _, payload ->

            val (content, rest) = payload.partition { item -> item.uri != null }

            content?.let {
                for (i in 0 until it.clip.itemCount) {
                    val item = it.clip.getItemAt(i).uri
                    onReceivedContent(item)
                }
            }
            rest
        }
    }
    AndroidView(
            modifier = modifier,
            factory = { ctx ->
                EditText(ctx).apply {
                    editTextBlock()
                    ViewCompat.setOnReceiveContentListener(
                        this, arrayOf("image/*", "video/*"), listener
                    )
                    addTextChangedListener {
                        onTextChange(it.toString())
                    }
                }
            },
            update = { view ->
                if (text != view.text.toString()) {
                    view.setText(text)
                }
            }
        )
}

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

@Preview
@Composable
fun RichContentEditTextPreview() {
    var text by remember {
        mutableStateOf("")
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Row (
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.End
        ) {
            RichContentEditText(
                modifier = Modifier.background(Color.DarkGray),
                text = text,
                onTextChange = { text = it },
                editTextBlock = {
                    setBackgroundColor(Color.Transparent.toArgb())
                },
                onReceivedContent = {

                },
            )
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "send",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}