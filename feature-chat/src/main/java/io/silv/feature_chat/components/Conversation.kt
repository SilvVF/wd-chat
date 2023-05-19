@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat.components

import android.net.Uri
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.feature_chat.ChatUiState
import io.silv.feature_chat.types.Chat
import io.silv.feature_chat.types.Message
import io.silv.feature_chat.types.MyChat
import io.silv.feature_chat.types.UiUserInfo
import io.silv.shared_ui.components.messageFormatter
import kotlinx.coroutines.launch

/**
 * Entry point for a conversation screen.
 *
 * @param uiState [ChatUiState.Success] that contains messages to display
 * @param modifier [Modifier] to apply to this layout node
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    uiState: ChatUiState.Success,
    modifier: Modifier = Modifier,
    onMessageChange: (String) -> Unit,
    onMessageSent: (String) -> Unit,
    onReceivedContent: (Uri) -> Unit,
    deleteAttachment: (Uri) -> Unit,
    navigateBack: () -> Unit
) {

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val users = remember(uiState.users.values) {
        uiState.users.values.toList()
    }
    val sortedMessages = remember(uiState.chats) {
        uiState.chats.reversed()
    }

    Scaffold(
        topBar = {
           TopAppBar(
               scrollBehavior = scrollBehavior,
               title = {
                      LazyRow {
                          items(
                              items = users,
                              key = { info -> info.id }
                          ) { info: UiUserInfo ->
                              Column {
                                  AsyncImage(
                                      modifier = Modifier
                                          .padding(horizontal = 16.dp)
                                          .size(30.dp)
                                          .border(
                                              1.5.dp,
                                              MaterialTheme.colorScheme.primary,
                                              CircleShape
                                          )
                                          .border(
                                              3.dp,
                                              MaterialTheme.colorScheme.surface,
                                              CircleShape
                                          )
                                          .clip(CircleShape),
                                      model = ImageRequest.Builder(LocalContext.current)
                                          .data(info.icon)
                                          .crossfade(true)
                                          .build(),
                                      contentScale = ContentScale.Crop,
                                      contentDescription = null,
                                      placeholder = painterResource(id = io.silv.shared_ui.R.drawable.ic_profile_default)
                                  )
                              }
                              Text(text = info.name, fontSize = 10.sp)
                          }
                      }
               },
               navigationIcon = {
                   IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back"
                        )
                   }
               }
           )
        },
        // Exclude ime and navigation bar padding so this can be added by the UserInput composable
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        val sendMessageEnabled =
            uiState.message.isNotBlank() || uiState.imageAttachments.isNotEmpty()

        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {
            Messages(
                messages = sortedMessages,
                userIdToInfo = uiState.users,
                modifier = Modifier.weight(1f),
                scrollState = scrollState,
            )
            UserInput(
                text = uiState.message,
                onTextChanged = { onMessageChange(it) } ,
                attachments = uiState.imageAttachments,
                attachmentsReceived = { uri ->
                       uri.forEach { onReceivedContent(it) }
                },
                onDeleteAttachment = { deleteAttachment(it) },
                sendChat = { onMessageSent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding()
            )
        }
    }
}

const val ConversationTestTag = "ConversationTestTag"

@Composable
fun Messages(
    messages: List<Chat>,
    userIdToInfo: Map<String, UiUserInfo>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {

        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
                .testTag(ConversationTestTag)
                .fillMaxSize()
        ) {
            itemsIndexed(messages) { index, chat ->
                val prevAuthor = messages.getOrNull(index - 1)?.message?.authorId
                val nextAuthor = messages.getOrNull(index + 1)?.message?.authorId
                val content = messages[index]
                val isFirstMessageByAuthor = prevAuthor != content.message.authorId
                val isLastMessageByAuthor = nextAuthor != content.message.authorId

                Message(
                    msg = content.message,
                    isUserMe = content is MyChat,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    isLastMessageByAuthor = isLastMessageByAuthor,
                    userIcon = userIdToInfo[content.message.authorId]?.icon
                )
            }
        }
        // Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Show the button if the first visible item is not the first one or if the offset is
        // greater than the threshold.
        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun Message(
    msg: Message,
    userIcon: Uri?,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean
) {
    val borderColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {
        if (isLastMessageByAuthor) {
            // Avatar
            AsyncImage(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(42.dp)
                    .border(1.5.dp, borderColor, CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .align(Alignment.Top),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userIcon)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                placeholder = painterResource(id = io.silv.shared_ui.R.drawable.ic_profile_default)
            )
        } else {
            // Space under avatar
            Spacer(modifier = Modifier.width(74.dp))
        }
        AuthorAndTextMessage(
            msg = msg,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ChatItemBubble(msg, isUserMe)
        if (isLastMessageByAuthor) {
            AuthorNameTimestamp(msg)
        }
        if (isFirstMessageByAuthor) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
@Composable
fun ChatItemBubble(
    message: Message,
    isUserMe: Boolean,
) {

    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column {
        if (message.content.isNotBlank()) {
            Surface(
                color = backgroundBubbleColor,
                shape = ChatBubbleShape
            ) {
                CopyableMessage(
                    message = message,
                    isUserMe = isUserMe,
                )
            }
        }

        message.images.forEach {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = backgroundBubbleColor,
                shape = ChatBubbleShape
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp),
                    contentDescription = "attached image"
                )
            }
        }
    }
}
@Composable
fun CopyableMessage(
    message: Message,
    isUserMe: Boolean,
) {
    val styledMessage = messageFormatter(
        text = message.content,
        primary = isUserMe
    )
    SelectionContainer {
        Text(
            text = styledMessage,
            style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
            modifier = Modifier.padding(16.dp),
        )
    }
}


@Composable
private fun AuthorNameTimestamp(msg: Message) {
    // Combine author and timestamp for a11y.
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = msg.author,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = msg.timestamp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alignBy(LastBaseline),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)


private enum class Visibility {
    VISIBLE,
    GONE
}


@Composable
fun JumpToBottom(
    enabled: Boolean,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Show Jump to Bottom button
    val transition = updateTransition(
        if (enabled) Visibility.VISIBLE else Visibility.GONE,
        label = "JumpToBottom visibility animation"
    )
    val bottomOffset by transition.animateDp(label = "JumpToBottom offset animation") {
        if (it == Visibility.GONE) {
            (-32).dp
        } else {
            32.dp
        }
    }
    if (bottomOffset > 0.dp) {
        ExtendedFloatingActionButton(
            icon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    modifier = Modifier.height(18.dp),
                    contentDescription = null
                )
            },
            text = {
                Text(text = "Jump to bottom")
            },
            onClick = onClicked,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = modifier
                .offset(x = 0.dp, y = -bottomOffset)
                .height(36.dp)
        )
    }
}



private val JumpToBottomThreshold = 56.dp