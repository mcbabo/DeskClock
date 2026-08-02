package app.grapheneos.deskclock.core.presentation.components.groupitems

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.Avatar
import app.grapheneos.deskclock.core.presentation.screenPadding
import app.grapheneos.deskclock.core.theme.DeskClockTheme

private const val MAX_LABEL_LENGTH = 30

/**
 * A [GroupRow] whose trailing content is a short, read-only value label —
 * e.g. a settings row that navigates to a picker or editor on tap and shows
 * the currently selected value (ringtone name, theme, snooze duration, etc.).
 * Fires a light confirmation haptic on tap.
 */
@Composable
fun ValueGroupRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    val view = LocalView.current

    GroupRow(
        modifier = modifier,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick()
        },
        leadingContent = leadingIcon,
        trailingContent = {
            Text(text = value)
        },
        supportingContent = supportingContent,
        verticalAlignment = Alignment.CenterVertically,
        content = { Text(label) }
    )
}

/**
 * A [GroupRow] whose trailing content is a [Switch]. Tapping anywhere on the
 * row toggles the switch (larger, thumb-friendly hit target); the switch
 * itself is display-only and ignores its own clicks, so there's a single
 * source of truth for the interaction. Fires TOGGLE_ON/TOGGLE_OFF haptics
 * matching the new state.
 */
@Composable
fun SwitchGroupRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val view = LocalView.current

    GroupRow(
        modifier = modifier,
        onClick = if (enabled) {
            {
                val newChecked = !checked
                if (newChecked) {
                    view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF)
                }
                onCheckedChange(newChecked)
            }
        } else {
            null
        },
        leadingContent = leadingIcon,
        supportingContent = supportingContent,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        verticalAlignment = Alignment.CenterVertically,
        content = { Text(label) }
    )
}

/**
 * A [GroupRow] whose trailing value can be edited in place: tapping the row
 * swaps the trailing [Text] for a focused [BasicTextField] (capped at
 * [MAX_LABEL_LENGTH] characters), and editing commits back to plain text
 * either by pressing the keyboard's Done action or by tapping away to defocus
 * the field. Used for short, single-line values like an alarm's label.
 */
@Composable
fun InlineEditableValueGroupRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    var isEditing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value) }
    var hasBeenFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    fun commit() {
        onValueChange(text.trim())
        isEditing = false
        hasBeenFocused = false
        keyboardController?.hide()
    }

    GroupRow(
        modifier = modifier,
        onClick = if (!isEditing) {
            {
                isEditing = true
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
        } else {
            null
        },
        leadingContent = leadingIcon,
        trailingContent = {
            if (isEditing) {
                BasicTextField(
                    value = text,
                    onValueChange = { newText ->
                        if (newText.length <= MAX_LABEL_LENGTH) {
                            text = newText
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    modifier = Modifier
                        .widthIn(min = 80.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasBeenFocused = true
                            } else if (hasBeenFocused) {
                                commit()
                            }
                        }
                )
            } else {
                Text(
                    text = value.ifBlank { placeholder },
                )
            }
        },
        content = { Text(label) }
    )

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }
}

/** Preview showcasing every row variant defined in this file. */
@Preview
@Composable
fun GroupedListPreview() {
    DeskClockTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.screenPadding(),
                verticalArrangement = Arrangement.spacedBy(Layout.SectionSpacing)
            ) {
                ListGroup(title = "Alarm Configuration") {
                    item(onClick = {}) {
                        ValueGroupRow(
                            label = "Alarm Label",
                            value = "Work",
                            onClick = {},
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null)
                            },
                            supportingContent = { Text("Morning Wakeup") }
                        )
                    }

                    item(onClick = {}) {
                        ValueGroupRow(
                            label = "Ringtone",
                            value = "Cesium",
                            onClick = {},
                            leadingIcon = {
                                Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
                            }
                        )
                    }

                    item {
                        InlineEditableValueGroupRow(
                            label = "Alarm Label",
                            value = "Work",
                            onValueChange = {},
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null)
                            },
                        )
                    }

                    item {
                        SwitchGroupRow(
                            label = "Vibrate",
                            checked = true,
                            onCheckedChange = {},
                            leadingIcon = {
                                Icon(Icons.Outlined.Vibration, contentDescription = null)
                            }
                        )
                    }
                }

                ListGroup(title = "Actions") {
                    item(onClick = {}) {
                        GroupRow(
                            content = { Text("Delete Alarm") },
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }

                ListGroup(title = "Basic items") {
                    repeat(2) { i ->
                        item(onClick = {}) {
                            GroupRow(content = { Text("Simple Item ${i + 1}") })
                        }
                    }
                }

                ListGroup(title = "Items with avatar") {
                    repeat(2) { i ->
                        item(onClick = {}) {
                            GroupRow(
                                content = { Text("Simple Item ${i + 1}") },
                                leadingContent = { Avatar("S") }
                            )
                        }
                    }
                }
            }
        }
    }
}
