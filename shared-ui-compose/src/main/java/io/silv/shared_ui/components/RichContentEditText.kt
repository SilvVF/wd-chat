package io.silv.shared_ui.components

import android.net.Uri
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    onReceivedContent:  (Uri) -> Unit = {},
    deleteAttachment: (Uri) -> Unit
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

