package com.yorkyang2333.claudwecho.ui.search

import android.app.Activity
import android.app.RemoteInput
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import com.yorkyang2333.claudwecho.ui.components.SharedSongItem
import com.yorkyang2333.claudwecho.ui.player.PlayerViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    playerViewModel: PlayerViewModel,
    onSongClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val context = LocalContext.current

    var fallbackInput by remember { mutableStateOf(false) }
    var localQuery by remember { mutableStateOf(searchQuery) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val results = RemoteInput.getResultsFromIntent(data)
                val query = results?.getCharSequence("search_query")?.toString()
                if (!query.isNullOrBlank()) {
                    viewModel.performSearch(query)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RotaryScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                Button(
                    onClick = {
                        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInputs = listOf(
                            RemoteInput.Builder("search_query")
                                .setLabel("搜索歌曲、歌手或专辑")
                                .build()
                        )
                        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                        try {
                            launcher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            // Fallback for standard Android devices or emulators without Wear OS Voice Input
                            fallbackInput = true
                            localQuery = searchQuery
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "搜索",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) searchQuery else "点击搜索",
                            modifier = Modifier.padding(start = 8.dp).weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (error != null) {
                item {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (searchResults.isNotEmpty()) {
                items(searchResults) { song ->
                    SharedSongItem(
                        song = song,
                        onClick = {
                            playerViewModel.playPlaylist(searchResults, searchResults.indexOf(song))
                            onSongClick()
                        }
                    )
                }
            }

            if (searchResults.isEmpty() && !isLoading && searchHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "历史搜索",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "清空",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.clearHistory() }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                }
                items(searchHistory) { query ->
                    Button(
                        onClick = {
                            viewModel.performSearch(query)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = "历史记录",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = query,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp).weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        if (fallbackInput) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicTextField(
                    value = localQuery,
                    onValueChange = { localQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(24.dp))
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            fallbackInput = false
                            if (localQuery.isNotBlank()) {
                                viewModel.performSearch(localQuery)
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "搜索",
                                modifier = Modifier.size(24.dp)
                            )
                            Box(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                                if (localQuery.isEmpty()) {
                                    Text(
                                        text = "输入搜索...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                com.yorkyang2333.claudwecho.ui.components.DialogActionButtons(
                    onCancel = {
                        keyboardController?.hide()
                        fallbackInput = false
                    },
                    onConfirm = {
                        keyboardController?.hide()
                        fallbackInput = false
                        if (localQuery.isNotBlank()) {
                            viewModel.performSearch(localQuery)
                        }
                    }
                )
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
        }
    }
}
