package com.example.tcgtracker.ui.screens

import android.graphics.Color.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.tcgtracker.R
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.ui.TrackerViewModel
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.PocketWhite
import kotlinx.serialization.Serializable

@Serializable
data object SetScreen: NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetScreen(
    onOptionsTap: () -> Unit,
    onSetTap: (String) -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Collect uiState
    val trackerUIState by trackerViewModel.uiState.collectAsState()

    // Get isListMode from current state
    var isListMode: Boolean by rememberSaveable {
        mutableStateOf(trackerUIState.isListMode)
    }

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
                    title = { Text("Pokémon TCG Pocket Tracker") },
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
            // Bottom bar
            bottomBar = {
                // UI color
                val uiColor = MaterialTheme.colorScheme.primaryContainer

                // Get most probable set and its associated color
                val setBooster = trackerUIState.setsData.getMostProbableSet(trackerUIState.originsData)
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
            // Content
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val series = trackerUIState.setsData.getSeriesMap()
                val colors = trackerUIState.setsData.getSetColors()

                series.forEach { element ->
                    val expansionsMap = getExpansionsMap(series, element.key)
                    SeriesGroup(
                        series = element.key,
                        expansions = expansionsMap,
                        colors = colors,
                        isListView = isListMode,
                        onSetTap = onSetTap
                    )
                }
            }
        }
    }
}

fun getExpansionsMap(
    map: Map<String, List<Set>>,
    series: String
): Map<String, List<Set>> {
    return map.getOrDefault(series, listOf()).groupBy({set -> set.expansion})
}

@Composable
fun SeriesGroup(
    series: String,
    expansions: Map<String, List<Set>>,
    colors: Map<String, Color>,
    isListView: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                    text = series,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            val spacing = if (isListView) 5.dp else 20.dp
            val verSpace = if (isListView) 20.dp else 40.dp
            val horSpace = if (isListView) 20.dp else 0.dp
            Column(
                Modifier.padding(horizontal = horSpace, verSpace),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                expansions.forEach { expansion ->
                    if (isListView) {
                        CollectionList(
                            expansion.value,
                            colors,
                            onSetTap,
                            Modifier.padding(horizontal = 20.dp)
                        )
                    } else {
                        CollectionRow(
                            expansion.value,
                            onSetTap,
                            Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionList(
    sets: List<Set>,
    colors: Map<String, Color>,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    sets.forEach { set ->
        val color: Color = colors[set.set] ?: MaterialTheme.colorScheme.primaryContainer
        CollectionCell(set, color, onSetTap)
    }
}

@Composable
fun CollectionCell(
    set: Set,
    color: Color,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fontColor = PocketBlack
    Box(
        modifier = modifier.background(color, RoundedCornerShape(10))
            .height(50.dp)
            .clickable{ onSetTap(set.set) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                // Top left
                Text(
                    modifier = Modifier.align(Alignment.TopStart).offset(y = (-5).dp),
                    text = set.name,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = fontColor
                )
                // Top right
                Text(
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-5).dp),
                    text = "${set.numbers.all.ownedCards} / ${set.numbers.all.totalCards}",
                    textAlign = TextAlign.Right,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = fontColor
                )
                // Bottom left
                Text(
                    modifier = Modifier.align(Alignment.BottomStart).offset(y = 6.dp),
                    text = set.set,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    color = fontColor
                )
                // Bottom right
                Text(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 6.dp),
                    text = "${set.numbers.all.ownedCards / set.numbers.all.totalCards * 100} %",
                    textAlign = TextAlign.Right,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    color = fontColor
                )
            }
        }
    }
}

@Composable
fun CollectionRow(
    sets: List<Set>,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var i = 0
        sets.forEach { set ->
            SetButton(
                collection = set.set,
                resourceID = set.cover.id,
                modifier = Modifier.weight(1f)
                    .clickable { onSetTap(set.set) }
            )

            i++
        }

        while(i < 3)
        {
            EmptyBox(
                modifier = Modifier.weight(1f)
            )

            i++
        }
    }
}

@Composable
fun SetButton(
    collection: String,
    resourceID: Int,
    modifier: Modifier = Modifier
) {
    val image = painterResource(resourceID)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            text = collection,
            modifier = Modifier.padding(bottom = 0.dp)
        )
    }
}

@Composable
fun EmptyBox(modifier: Modifier = Modifier) {
    Box(
        modifier
    ) {

    }
}