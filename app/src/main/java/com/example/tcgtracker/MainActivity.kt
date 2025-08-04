package com.example.tcgtracker

import android.graphics.Color.alpha
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgtracker.ui.TrackerUIState
import com.example.tcgtracker.ui.screens.Navigation
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
                Navigation(applicationContext)
            }
        }
    }
}

enum class Screen {
    SetSelector,
    CardViewer,
    Options
}

/*
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
                    navigateUp = {
                        navController.navigateUp()
                    },
                    canNavigateToOptions = currentScreen != Screen.Options,
                    navigateToOptions = {
                        navController.navigate(Screen.Options.name)
                    },
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
                    val cardList = trackerViewModel.getPrettyCardsList(context)
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
                    OptionsScreen(
                        context = context,
                        scope = scope,
                        onCardsImported = { importedSets ->
                            trackerViewModel.reloadOwnedCardState(context, importedSets)
                        },
                        snackbarHostState = snackbarHostState
                    )
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
                        painter = painterResource(R.drawable.arrow_back),
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
                        painter = painterResource(R.drawable.view_array),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
            }
            if (canNavigateToOptions) {
                IconButton(onClick = navigateToOptions) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
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

    val setBooster = uiState.setsData.getMostProbableSet(uiState.originsData)
    val setColor = setBooster?.first?.color ?: MaterialTheme.colorScheme.surface

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
                painter = painterResource(R.drawable.info),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
            Box(
                modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth(0.75f)
                    .background(setColor, RoundedCornerShape(percent = 50))
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(percent = 50),
                        clip = true,
                        ambientColor = Color(0.0f, 0.0f, 0.0f, 0.0f),
                        spotColor = PocketWhite.apply{ alpha(100) }
                    )
                    .border(2.dp, uiColor.apply{ alpha(50) }, RoundedCornerShape(percent = 50))
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
            ) {
                Text(
                    text = setBooster?.second?.name ?: "",
                    textAlign = TextAlign.Center,
                    style = TextStyle.Default.copy(
                        fontSize = 26.sp,
                        color = PocketWhite,
                        shadow = Shadow(
                            color = PocketBlack,
                            blurRadius = 10.0f
                        )
                    )
                )
            }

            Icon(
                painter = painterResource(R.drawable.filter_alt),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}

 */