package com.example.tcgtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

class TwoPaneScene<T: Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val firstEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>
): Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(firstEntry, secondEntry)
    override val content: @Composable (() -> Unit) = {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1.0f)
            ) {
                firstEntry.content(firstEntry.key)
            }
            Box(
                modifier = Modifier.weight(1.0f)
            ) {
                secondEntry.content(secondEntry.key)
            }
        }
    }

    companion object {
        internal const val TWO_PANE_KEY = "TwoPane"
        fun twoPane() = mapOf(TWO_PANE_KEY to true)
    }
}

class TwoPaneSceneStrategy<T: Any>: SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        if(!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val lastTwoEntries = entries.takeLast(2)
        return if (lastTwoEntries.size == 2 && lastTwoEntries.all{ it.metadata.contains(TwoPaneScene.TWO_PANE_KEY) }) {
            val firstEntry = lastTwoEntries[0]
            val secondEntry = lastTwoEntries[1]

            TwoPaneScene(
                key = firstEntry.key to secondEntry.key,
                previousEntries = entries.dropLast(1),
                firstEntry,
                secondEntry
            )
        } else null
    }
}