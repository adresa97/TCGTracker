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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.tcgtracker.models.Concepts
import com.example.tcgtracker.models.SQLiteHandler
import com.example.tcgtracker.models.OriginsData
import com.example.tcgtracker.models.SetsData
import com.example.tcgtracker.ui.screens.binder.CardsScreen
import com.example.tcgtracker.ui.screens.binder.SetScreen
import com.example.tcgtracker.ui.screens.options.OptionsScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Navigation (
    context: Context
) {
    // Load singleton objects data
    val handler = SQLiteHandler(context)
    Concepts.loadJSONData(context)
    OriginsData.loadJSONData(context)
    SetsData.loadJSONData(context, handler)

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
                        handler = handler,
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
                        handler = handler,
                        onBackTap = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        )
    }
}