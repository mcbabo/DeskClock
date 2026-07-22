package app.grapheneos.deskclock.clock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import app.grapheneos.deskclock.R
import app.grapheneos.deskclock.core.presentation.Layout
import app.grapheneos.deskclock.core.presentation.components.groupitems.GroupRow
import app.grapheneos.deskclock.core.presentation.components.groupitems.groupHeader
import app.grapheneos.deskclock.core.presentation.components.groupitems.lazyGroup
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockSearch(
    filteredZones: Map<Char, List<ZoneId>>,
    searchListState: LazyListState,
    searchBarState: SearchBarState,
    inputField: @Composable (() -> Unit),
    onClick: (ZoneId) -> Unit
) {
    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField
    ) {
        LazyColumn(
            state = searchListState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(Layout.GroupedList.ItemSpacing),
            contentPadding = Layout.contentPadding()
        ) {
            filteredZones.forEach { (char, zones) ->
                groupHeader(title = char.toString(), key = "header_$char")

                lazyGroup(
                    items = zones,
                    key = { it.id },
                    onClick = { onClick(it) }
                ) { zone ->
                    GroupRow(
                        content = {
                            Text(zone.id.substringAfter('/').replace('_', ' '))
                        },
                        supportingContent = {
                            Text(
                                text = zone.id.substringBefore('/'),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarInput(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onBack: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBarDefaults.InputField(
        modifier = Modifier,
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        onSearch = { keyboardController?.hide() },
        placeholder = { Text(text = stringResource(R.string.search)) },
        leadingIcon = {
            if (searchBarState.currentValue == SearchBarValue.Expanded) {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search),
                )
            }
        }
    )
}
