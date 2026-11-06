package com.boogie_knight.tcgtracker.ui.screens.binder

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.boogie_knight.tcgtracker.R
import com.boogie_knight.tcgtracker.models.Game
import com.boogie_knight.tcgtracker.models.GameIcon
import com.boogie_knight.tcgtracker.models.Set
import com.boogie_knight.tcgtracker.services.OriginsData
import com.boogie_knight.tcgtracker.services.SetsData
import com.boogie_knight.tcgtracker.ui.TrackerViewModel
import com.boogie_knight.tcgtracker.ui.theme.PocketBlack
import com.boogie_knight.tcgtracker.ui.theme.getSimilarColor
import com.boogie_knight.tcgtracker.utils.greyScale
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object GameScreen: NavKey

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onOptionsTap: () -> Unit,
    onGameTap: (String) -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Collect uiState
    val trackerUIState by trackerViewModel.uiState.collectAsState()

    // Get isListMode from current state
    var isListMode: Boolean by rememberSaveable {
        mutableStateOf(trackerUIState.isListMode)
    }

    // UI color
    val uiColor = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            // Top bar
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = uiColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = { Text(text = "TCG Tracker", style = MaterialTheme.typography.titleLarge) },
                    modifier = Modifier,
                    actions = {
                        IconButton(
                            onClick = {
                                trackerViewModel.changeViewMode()
                                isListMode = !isListMode
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.view_array),
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                onOptionsTap()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.settings),
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            // Bottom sheet
            bottomBar = { }
            // Content
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val games = trackerViewModel.getGameIconsMap()
                if (games.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(all = 10.dp),
                        text = "No se ha encontrado información.\n" +
                                "Por favor conecte el dispositivo a intenet.\n" +
                                "Si persiste, hay problemas en el repositorio",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    )
                } else {
                    val screenSize = LocalConfiguration.current.screenHeightDp
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            GamesContent(
                                games = games,
                                screenSize = screenSize,
                                isListView = isListMode,
                                onGameTap = { game -> onGameTap(game) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamesContent(
    games: Map<String, GameIcon>,
    screenSize: Int,
    isListView: Boolean,
    onGameTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isListView) {
        Column(
            modifier = Modifier
                .padding(all = 30.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            games.forEach { game ->
                GameCell(
                    gameName = game.value.name,
                    color = game.value.color,
                    modifier = Modifier.clickable{ onGameTap(game.key) }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .padding(all = 30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            var rowHeight = (screenSize.toFloat() / 6.0f).dp
            if (rowHeight < 100.dp) rowHeight = 100.dp

            games.forEach { game ->
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(rowHeight),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameButton(
                        gameName = game.value.name,
                        logo = game.value.logo,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { onGameTap(game.key) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(
    gameName: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val backColor = getSimilarColor(
        color = color,
        minSaturation = 0.4f,
        maxSaturation = 0.6f,
        value = 0.9f
    )

    Box(
        modifier = modifier.background(backColor, RoundedCornerShape(10))
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                // Center
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = gameName,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                )
            }
        }
    }
}

@Composable
fun GameButton(
    gameName: String,
    logo: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val backColor = getSimilarColor(
        color = color,
        minSaturation = 0.2f,
        maxSaturation = 0.3f,
        value = 0.4f
    )

    Box(
        modifier = modifier.background(backColor, RoundedCornerShape(10))
            .fillMaxSize().padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        var isLoaded: Boolean by rememberSaveable {
            mutableStateOf(false)
        }
        AsyncImage(
            modifier = Modifier
                .fillMaxSize().padding(vertical = 12.dp, horizontal = 12.dp),
            model = logo,
            onSuccess = { isLoaded = true },
            contentDescription = gameName,
            contentScale = ContentScale.Fit
        )
        if (!isLoaded) {
            Text(
                text = gameName,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxHeight(0.2f)
                    .padding(bottom = 0.dp)
            )
        }
    }

}