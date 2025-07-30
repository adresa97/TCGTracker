package com.example.tcgtracker

import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.tcgtracker.ui.theme.TCGTrackerTheme
import com.example.tcgtracker.ui.theme.setColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

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

    trackerViewModel.loadCollectionsJSON(context)
    val trackerUIState by trackerViewModel.uiState.collectAsState()
    LocalLifecycleOwner.current.lifecycle.addObserver(trackerViewModel)

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
                    navigateToOptions = { navController.navigate(Screen.Options.name) }
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
            )
            {
                composable(Screen.SetSelector.name) {
                    SetScreen(
                        series = trackerUIState.setsData.getSeriesMap(),
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
    modifier: Modifier = Modifier
) {
    val title: String
    when (currentScreen) {
        Screen.SetSelector -> title = "Pokémon TCG Pocket Tracker"
        Screen.CardViewer -> title = uiState.setsData.getSetName(uiState.selectedSet)
        Screen.Options -> title = "Opciones"
        else -> title = ""
    }

    val currentSet = uiState.selectedSet.substringBefore('-')
    val color = if (currentScreen == Screen.CardViewer) {
        setColors[currentSet] ?: MaterialTheme.colorScheme.primaryContainer
    } else MaterialTheme.colorScheme.primaryContainer

    TopAppBar(
        colors = topAppBarColors(
            containerColor = color,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = { Text(title)},
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
            if (canNavigateToOptions) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
                IconButton(onClick = navigateToOptions) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
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
    val currentSet = uiState.selectedSet.substringBefore('-')
    val color = if (currentScreen == Screen.CardViewer) {
        setColors[currentSet] ?: MaterialTheme.colorScheme.primaryContainer
    } else MaterialTheme.colorScheme.primaryContainer

    BottomAppBar(
        containerColor = color,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = ""
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun TCGTrackerPreview() {
    val collections = SetsData()
    val service = TCGDexService("en")

    TCGTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("TCGTracker")
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Bottom bar"
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding))
            {
                //SetSelectorList(collections.getSeriesMap())
            }
        }
    }
}