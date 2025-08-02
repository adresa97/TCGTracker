package com.example.tcgtracker

import android.content.Context
import android.graphics.Color.alpha
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tcgtracker.ui.CardsScreen
import com.example.tcgtracker.ui.OptionsScreen
import com.example.tcgtracker.ui.SetScreen
import com.example.tcgtracker.ui.TrackerUIState
import com.example.tcgtracker.ui.TrackerViewModel
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.PocketWhite
import com.example.tcgtracker.ui.theme.TCGTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Make it Asynchronous
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TCGTrackerTheme {
                TCGTrackerApp(applicationContext)
            }
        }
    }
}

enum class Screen {
    SetSelector,
    CardViewer,
    Options
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCGTrackerApp(
    context: Context,
    trackerViewModel: TrackerViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.SetSelector.name
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    trackerViewModel.loadData(context)
    val trackerUIState by trackerViewModel.uiState.collectAsState()
    LocalLifecycleOwner.current.lifecycle.addObserver(trackerViewModel)

    var isListMode: Boolean by rememberSaveable {
        mutableStateOf(trackerUIState.isListMode)
    }

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            topBar = {
                TCGTrackerTopBar(
                    currentScreen = currentScreen,
                    uiState = trackerUIState,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() },
                    canNavigateToOptions = currentScreen != Screen.Options,
                    navigateToOptions = { navController.navigate(Screen.Options.name) },
                    canChangeViewMode = currentScreen != Screen.Options,
                    changeViewMode = {
                        trackerViewModel.changeViewMode()
                        isListMode = !isListMode
                    }
                )
            },
            bottomBar = {
                TCGTrackerBottomBar(
                    currentScreen = currentScreen,
                    uiState = trackerUIState
                )
            },
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
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = Screen.SetSelector.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.SetSelector.name) {
                    SetScreen(
                        series = trackerUIState.setsData.getSeriesMap(),
                        isListView = isListMode,
                        colors = trackerUIState.setsData.getSetColors(),
                        onSetTap = {
                            trackerViewModel.setCurrentSet(it)
                            navController.navigate(Screen.CardViewer.name)
                        }
                    )
                }

                composable(Screen.CardViewer.name) {
                    val cardList = trackerViewModel.getCardsList(context)
                    CardsScreen(
                        cardList = cardList,
                        isListMode = isListMode,
                        colors = trackerUIState.originsData.getOriginsNameColorMap(),
                        onCardTap = { cardIndex ->
                            trackerViewModel.changeOwnedCardState(
                                context,
                                trackerUIState.selectedSet,
                                cardIndex
                            )
                        }
                    )
                }

                composable(Screen.Options.name) {
                    OptionsScreen(context, scope, snackbarHostState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCGTrackerTopBar(
    currentScreen: Screen,
    uiState: TrackerUIState,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    canNavigateToOptions: Boolean,
    navigateToOptions: () -> Unit = {},
    canChangeViewMode: Boolean,
    changeViewMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val title = when (currentScreen) {
        Screen.SetSelector -> "Pokémon TCG Pocket Tracker"
        Screen.CardViewer -> uiState.setsData.getSetName(uiState.selectedSet)
        Screen.Options -> "Opciones"
        //else -> title = ""
    }

    val uiColor = when (currentScreen) {
        Screen.CardViewer -> {
            uiState.setsData.getSetColor(uiState.selectedSet)
                ?: MaterialTheme.colorScheme.primaryContainer
        }
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    TopAppBar(
        colors = topAppBarColors(
            containerColor = uiColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = { Text( title ) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (canChangeViewMode) {
                IconButton(onClick = changeViewMode) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
            }
            if (canNavigateToOptions) {
                IconButton(onClick = navigateToOptions) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TCGTrackerBottomBar(
    currentScreen: Screen,
    uiState: TrackerUIState
) {
    val uiColor = when (currentScreen) {
        Screen.CardViewer -> {
            uiState.setsData.getSetColor(uiState.selectedSet)
                ?: MaterialTheme.colorScheme.primaryContainer
        }
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val setColor = uiState.setsData.getSetColor("A3b")
        ?: MaterialTheme.colorScheme.surface

    BottomAppBar(
        containerColor = uiColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
            Text(
                modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth(0.75f)
                    .background(setColor, RoundedCornerShape(percent = 50))
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(percent = 50),
                        clip = true,
                        ambientColor = Color(0.0f, 0.0f, 0.0f, 0.0f),
                        spotColor = PocketWhite.apply{ alpha(100) }
                    )
                    .border(2.dp, PocketBlack.apply{ alpha(50) }, RoundedCornerShape(percent = 50))
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = "Eevee",
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Filled.Warning,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}