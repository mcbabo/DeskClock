package app.grapheneos.deskclock.alarm.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupItem
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.ringtone.RingtoneItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonePickerDialog(
    ringtones: List<RingtoneItem>,
    initialUri: String?,
    onPlayPreview: (String) -> Unit,
    onStopPreview: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedUri by remember { mutableStateOf(initialUri ?: ringtones.firstOrNull()?.uri ?: "") }

    DisposableEffect(Unit) {
        onDispose { onStopPreview() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        )
    ) {
        RingtonePickerDialogContent(
            ringtones = ringtones,
            selectedUri = selectedUri,
            onPlayPreview = onPlayPreview,
            onSelectUri = { selectedUri = it },
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun RingtonePickerDialogContent(
    ringtones: List<RingtoneItem>,
    selectedUri: String,
    onSelectUri: (String) -> Unit,
    onPlayPreview: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ringtones)) },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.dismiss)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onConfirm(selectedUri) }) {
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
            itemsIndexed(ringtones) { index, ringtoneItem ->
                val isSelected = ringtoneItem.uri == selectedUri
                GroupItem(
                    index = index,
                    count = ringtones.size,
                    onClick = {
                        onSelectUri(ringtoneItem.uri)
                        onPlayPreview(ringtoneItem.uri)
                    }
                ) {
                    GroupRow(
                        content = { Text(ringtoneItem.name) },
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
            }
        }
    }
}

@Preview
@Composable
fun RingtonePickerDialogPreview() {
    RingtonePickerDialogContent(
        ringtones = listOf(
            RingtoneItem(
                "Neon",
                "neon"
            ),
            RingtoneItem(
                "Cesium",
                "cesium"
            ),
            RingtoneItem(
                "Oxygen",
                "oxygen"
            )
        ),
        selectedUri = "cesium",
        onSelectUri = {},
        onPlayPreview = {},
        onConfirm = {},
        onDismiss = {}
    )
}
