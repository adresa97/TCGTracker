package com.boogie_knight.tcgtracker.ui.screens.options

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.boogie_knight.tcgtracker.R
import com.boogie_knight.tcgtracker.services.UserOptions
import com.boogie_knight.tcgtracker.ui.TrackerViewModel
import com.boogie_knight.tcgtracker.ui.theme.getSimilarColor
import com.boogie_knight.tcgtracker.utils.GetCustomContents
import com.boogie_knight.tcgtracker.utils.SetCustomContent
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object OptionsScreen: NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    context: Context,
    onBackTap: () -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Get coroutine scope and host state of snackbar
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            // Top bar
            topBar = {
                // UI color
                val uiColor = MaterialTheme.colorScheme.primaryContainer

                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = uiColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = { Text(text = "Opciones", style = MaterialTheme.typography.titleLarge) },
                    modifier = Modifier,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onBackTap()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            // Snackbar
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(data.visuals.message)
                    }
                }
            },
            // Content
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Delegate activity file picker for json files
                val jsonPicker = rememberLauncherForActivityResult(
                    contract = GetCustomContents(isMultiple = false),
                    onResult = { uris ->
                            val message =
                                ImporterExporter.importFromJSON(context, uris[0])
                            if (!message.second.isNullOrEmpty()) {
                                message.second!!.forEach{ game->
                                    if (!game.value.isEmpty()) trackerViewModel.reloadOwnedCardState(game.key, game.value)
                                }
                            }

                        scope.launch {
                            snackbarHostState.showSnackbar(message.first)
                        }
                    }
                )

                val storePicker = rememberLauncherForActivityResult(
                    contract = SetCustomContent(),
                    onResult = { uris ->
                            val message =
                                ImporterExporter.exportToJSON(context, uris[0])

                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )

                val startingScreens = trackerViewModel.getStartScreenMap()
                val userOption = startingScreens[trackerViewModel.getUserStartScreen().first]
                var selected by remember {
                    mutableStateOf(userOption ?: UserOptions.StartScreen.SELECTOR.screenName)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OptionButton (
                        onTap = { jsonPicker.launch("application/json") },
                        icon = R.drawable.download,
                        text = "Importar datos"
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .alpha(0.75f),
                        thickness = 2.dp,
                        color = getSimilarColor(
                            color = MaterialTheme.colorScheme.surface,
                            saturation = 0.1f,
                            value = 0.1f
                        )
                    )
                    OptionButton (
                        onTap = { storePicker.launch("application/json") },
                        icon = R.drawable.upload,
                        text = "Exportar datos"
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .alpha(0.75f),
                        thickness = 2.dp,
                        color = getSimilarColor(
                            color = MaterialTheme.colorScheme.surface,
                            saturation = 0.1f,
                            value = 0.1f
                        )
                    )
                    OptionSelector (
                        onTap = { startingScreen ->
                            if (startingScreens.containsKey(startingScreen)) {
                                trackerViewModel.saveStartScreen(startingScreen)
                                selected = startingScreens[startingScreen]!!
                            }
                        },
                        icon = R.drawable.home,
                        text = "Pantalla de incio",
                        options = startingScreens,
                        selectedOption = selected
                    )
                }
            }
        }
    }
}

@Composable
fun OptionButton(
    onTap: () -> Unit = {},
    icon: Int,
    text: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable(onClick = onTap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 5.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Text(
            text = text,
            fontSize = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSelector(
    onTap: (String) -> Unit,
    icon: Int,
    text: String = "",
    selectedOption: String,
    options: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val dropdownTextColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primaryContainer
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier.padding(horizontal = 5.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
            Text(
                text = text,
                fontSize = 20.sp
            )
        }
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(1.0f).fillMaxHeight().padding(all = 0.dp),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Box() {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .padding(vertical = 5.dp),
                    readOnly = true,
                    singleLine = true,
                    maxLines = 1,
                    minLines = 1,
                    value = "",
                    onValueChange = { },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = dropdownTextColors
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .height(50.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    text = selectedOption,
                    textAlign = TextAlign.Left,
                    fontSize = 14.sp,
                    maxLines = 1,
                    minLines = 1
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach{ option->
                    DropdownMenuItem(
                        text = { Text(text = option.value, fontSize = 14.sp) },
                        onClick = {
                            onTap(option.key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}