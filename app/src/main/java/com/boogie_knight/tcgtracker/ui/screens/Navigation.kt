package com.boogie_knight.tcgtracker.ui.screens

import android.content.Context
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.boogie_knight.tcgtracker.services.UserOptions
import com.boogie_knight.tcgtracker.ui.TrackerViewModel
import com.boogie_knight.tcgtracker.ui.screens.binder.CardsScreen
import com.boogie_knight.tcgtracker.ui.screens.binder.GameScreen
import com.boogie_knight.tcgtracker.ui.screens.binder.SetScreen
import com.boogie_knight.tcgtracker.ui.screens.options.OptionsScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Navigation (
    context: Context,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Load data
    trackerViewModel.loadData(context)

    // Get initial screen from stored user options
    val startingOptions = trackerViewModel.getUserStartScreen()
    var initialScreen = ""
    if (startingOptions.first != UserOptions.StartScreen.SELECTOR.name) {
        if (trackerViewModel.isGameInDatabase(startingOptions.first)) initialScreen = startingOptions.first
        else if (trackerViewModel.isGameInDatabase(startingOptions.second)) initialScreen = startingOptions.second
    }

    // Create navigation backstack
    val backStack = rememberNavBackStack(GameScreen)
    if (initialScreen != "") backStack.add(SetScreen(initialScreen))

    // Navigation
    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                ContentTransform(
                    slideInHorizontally(initialOffsetX = { it }),
                    slideOutHorizontally(targetOffsetX = { -it })
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    slideInHorizontally(initialOffsetX = { -it }),
                    slideOutHorizontally(targetOffsetX = { it })
                )
            },
            // sceneStrategy = rememberListDetailSceneStrategy(),
            entryProvider = entryProvider {
                entry<GameScreen>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Text("Elige un juego para ver sus colecciones")
                        }
                    )
                ) {
                    GameScreen(
                        onOptionsTap = {
                            backStack.add(OptionsScreen)
                        },
                        onGameTap = { selectedGame ->
                            backStack.add(SetScreen(selectedGame))
                            trackerViewModel.saveLastOpened(selectedGame)
                        }
                    )
                }
                entry<SetScreen>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Text("Elige un set para ver sus cartas")
                        }
                    )
                ) { currentGame ->
                    SetScreen(
                        currentGame = currentGame.currentGame,
                        onBackTap = {
                            backStack.removeLastOrNull()
                        },
                        onOptionsTap = {
                            backStack.add(OptionsScreen)
                        },
                        onSetTap = { currentGame: String, selectedSet: String ->
                            backStack.add(CardsScreen(currentGame, selectedSet))
                        }
                    )
                }
                entry<CardsScreen>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { currentSet ->
                    CardsScreen(
                        currentGame = currentSet.currentGame,
                        currentSet = currentSet.currentSet,
                        onBackTap = {
                            backStack.removeLastOrNull()
                        },
                        onOptionsTap = {
                            backStack.add(OptionsScreen)
                        }
                    )
                }
                entry<OptionsScreen> {
                    OptionsScreen(
                        context = context,
                        onBackTap = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        )

        // Loading Overlay
        if (trackerViewModel.isLoading()) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .alpha(0.6f)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}