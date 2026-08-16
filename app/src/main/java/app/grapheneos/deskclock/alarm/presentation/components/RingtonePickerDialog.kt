package app.grapheneos.deskclock.alarm.presentation.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupItem
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.groupHeader
import app.grapheneos.deskclock.core.presentation.components.groupitems.lazyGroup
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.ringtone.RingtoneItem

@Immutable
data class RingtonePickerUiState(
    val ringtones: List<RingtoneItem> = emptyList(),
    val selectedUri: Uri? = null,
    val isDirectBootOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonePickerDialog(
    uiState: RingtonePickerUiState,
    onPlayPreview: (Uri) -> Unit,
    onStopPreview: () -> Unit,
    onImportRingtone: () -> Unit,
    onDeleteRingtone: (RingtoneItem) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Uri) -> Unit
) {
    var selectedUri by remember(uiState.selectedUri) { mutableStateOf(uiState.selectedUri) }
    var ringtoneToDelete by remember { mutableStateOf<RingtoneItem?>(null) }

    DisposableEffect(Unit) {
        onDispose { onStopPreview() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val listState = rememberLazyListState()
        val selectedUriObj = selectedUri

        val customRingtones = uiState.ringtones.filter { it.uri.scheme == "file" }
        val systemRingtones = uiState.ringtones.filter { it.uri.scheme == "content" }
        val internalRingtones = uiState.ringtones.filter { it.uri.scheme == "android.resource" }

        val customTitle = stringResource(R.string.custom)
        val systemTitle = stringResource(R.string.system)
        val internalTitle = stringResource(R.string.internal)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.ringtones)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.dismiss)
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { selectedUri?.let { onConfirm(it) } }) {
                            Text(stringResource(R.string.save))
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .screenPadding(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing)
            ) {
                val showCustomSection = !uiState.isDirectBootOnly || customRingtones.isNotEmpty()
                if (showCustomSection) {
                    groupHeader(customTitle)
                    val customCount = if (!uiState.isDirectBootOnly) customRingtones.size + 1 else customRingtones.size

                    if (!uiState.isDirectBootOnly) {
                        item {
                            GroupItem(index = 0, count = customCount, onClick = onImportRingtone) {
                                GroupRow(
                                    content = { Text(stringResource(R.string.import_ringtone)) },
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        }
                    }

                    itemsIndexed(customRingtones, key = { _, item -> item.uri }) { index, ringtoneItem ->
                        val adjustedIndex = if (!uiState.isDirectBootOnly) index + 1 else index
                        GroupItem(
                            index = adjustedIndex,
                            count = customCount,
                            onClick = {
                                selectedUri = ringtoneItem.uri
                                onPlayPreview(ringtoneItem.uri)
                            },
                            onLongClick = {
                                ringtoneToDelete = ringtoneItem
                            }
                        ) {
                            RingtoneRow(
                                item = ringtoneItem,
                                isSelected = ringtoneItem.uri == selectedUriObj
                            )
                        }
                    }
                }

                if (systemRingtones.isNotEmpty()) {
                    lazyGroup(
                        items = systemRingtones,
                        title = systemTitle,
                        key = { it.uri },
                        onClick = {
                            selectedUri = it.uri
                            onPlayPreview(it.uri)
                        }
                    ) { ringtoneItem ->
                        RingtoneRow(
                            item = ringtoneItem,
                            isSelected = ringtoneItem.uri == selectedUriObj
                        )
                    }
                }

                if (internalRingtones.isNotEmpty()) {
                    lazyGroup(
                        items = internalRingtones,
                        title = internalTitle,
                        key = { it.uri },
                        onClick = {
                            selectedUri = it.uri
                            onPlayPreview(it.uri)
                        }
                    ) { ringtoneItem ->
                        RingtoneRow(
                            item = ringtoneItem,
                            isSelected = ringtoneItem.uri == selectedUriObj
                        )
                    }
                }
            }
        }
    }

    ringtoneToDelete?.let { ringtone ->
        AlertDialog(
            onDismissRequest = { ringtoneToDelete = null },
            title = { Text(stringResource(R.string.delete_ringtone)) },
            text = { Text(stringResource(R.string.delete_ringtone_confirmation, ringtone.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRingtone(ringtone)
                        ringtoneToDelete = null
                        if (selectedUri == ringtone.uri) {
                            selectedUri = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { ringtoneToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RingtoneRow(
    item: RingtoneItem,
    isSelected: Boolean
) {
    GroupRow(
        content = { Text(item.name) },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun RingtonePickerDialogPreview() {
    Scaffold {
        RingtonePickerDialog(
            uiState = RingtonePickerUiState(
                ringtones = listOf(
                    RingtoneItem("Neon", "content://neon".toUri()),
                    RingtoneItem("Cesium", "android.resource://cesium".toUri()),
                    RingtoneItem("Oxygen", "content://oxygen".toUri())
                ),
                selectedUri = "android.resource://cesium".toUri()
            ),
            onPlayPreview = {},
            onStopPreview = {},
            onImportRingtone = {},
            onDeleteRingtone = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
