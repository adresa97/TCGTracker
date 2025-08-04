package com.example.tcgtracker.ui.screens

import android.content.Context
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.tcgtracker.ui.TrackerViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Navigation (
    context: Context,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Load view model data and set onPause observer
    trackerViewModel.loadData(context)
    LocalLifecycleOwner.current.lifecycle.addObserver(trackerViewModel)

    // Create navigation backstack
    val backStack = rememberNavBackStack(SetScreen)

    // Navigation
    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                //rememberViewModelStoreNavEntryDecorator()
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
                entry<SetScreen>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Text("Elige un set para ver sus cartas")
                        }
                    )
                ) {
                    SetScreen(
                        onOptionsTap = {
                            backStack.add(OptionsScreen)
                        },
                        onSetTap = { selectedSet ->
                            backStack.add(CardsScreen(selectedSet))
                        }
                    )
                }
                entry<CardsScreen>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { currentSet ->
                    CardsScreen(
                        context = context,
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
    }
}