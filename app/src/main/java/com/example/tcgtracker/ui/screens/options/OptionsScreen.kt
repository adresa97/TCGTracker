package com.example.tcgtracker.ui.screens.options

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.tcgtracker.R
import com.example.tcgtracker.ui.TrackerViewModel
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.getSimilarColor
import com.example.tcgtracker.utils.GetCustomContents
import com.example.tcgtracker.utils.SetCustomContent
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
                    title = { Text("Opciones") },
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
                                trackerViewModel.reloadOwnedCardState(
                                    context,
                                    message.second!!
                                )
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
            fontSize = 24.sp
        )
    }
}