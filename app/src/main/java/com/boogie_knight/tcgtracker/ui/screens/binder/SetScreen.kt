package com.boogie_knight.tcgtracker.ui.screens.binder

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

    // Get active rarities from FiltersManager
    var currentRarityFilters: List<String> by rememberSaveable {
        mutableStateOf(FiltersManager.getActiveRarityFilters())
    }

    // Get active sets from FiltersManager
    var currentSetFilters: List<String> by rememberSaveable {
        mutableStateOf(FiltersManager.getActiveSetFilters())
    }

    // UI color
    val uiColor = MaterialTheme.colorScheme.primaryContainer

    // Bottom sheet state
    val scaffoldScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var isSheetExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    var isFiltersSheet: Boolean by rememberSaveable { mutableStateOf(false) }

    val bottomBarHeight = 75.dp
    val safeArea = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        BottomSheetScaffold(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    scaffoldScope.launch {
                        if (isSheetExpanded) {
                            scaffoldState.bottomSheetState.partialExpand()
                            isSheetExpanded = false
                        }
                    }
                })
            },
            scaffoldState = scaffoldState,
            // Top bar
            topBar = {
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
            // Bottom sheet
            sheetDragHandle = {},
            sheetSwipeEnabled = false,
            sheetPeekHeight = bottomBarHeight + safeArea,
            sheetContainerColor = uiColor,
            sheetContentColor = uiColor,
            sheetTonalElevation = 10.dp,
            sheetShadowElevation = 10.dp,
            sheetContent = {
                // Get most probable set and its associated color
                val setBooster = trackerViewModel.getMostProbableSetFromList(currentSetFilters, currentRarityFilters)
                val setColor = setBooster?.first?.color ?: MaterialTheme.colorScheme.surface

                BottomSheet(
                    title = setBooster?.second?.name?.es ?: "",
                    uiColor = uiColor,
                    trackerColor = setColor,
                    peekArea = bottomBarHeight,
                    safeArea = safeArea,
                    isFiltersSheet = isFiltersSheet,
                    isAlreadyFiltered = !FiltersManager.areAllRarityFiltersActivated(currentRarityFilters),
                    onIconClick = { isFilters ->
                        scaffoldScope.launch {
                            if (!isSheetExpanded) {
                                isFiltersSheet = isFilters
                                isSheetExpanded = true
                                scaffoldState.bottomSheetState.expand()
                            } else {
                                if (isFiltersSheet == isFilters) {
                                    scaffoldState.bottomSheetState.partialExpand()
                                    isSheetExpanded = false
                                } else {
                                    isFiltersSheet = isFilters
                                }
                            }
                        }
                    },
                    onFiltersChanged = {
                        currentRarityFilters = FiltersManager.getActiveRarityFilters()
                    },
                    infoScreen = {
                        val seriesMap = trackerViewModel.getSeriesMap()
                        InfoSetSheet(
                            sets = seriesMap,
                            activeSets = currentSetFilters,
                            onSetClick = {
                                currentSetFilters = FiltersManager.getActiveSetFilters()
                            },
                            rarities = currentRarityFilters
                        )
                    }
                )
            }
            // Content
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val series = trackerViewModel.getSeriesMap()
                val colors = trackerViewModel.getSetColors()

                if (series.isEmpty()) {
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
                    series.forEach { element ->
                        val expansionsMap = getExpansionsMap(series, element.key)
                        val screenSize = LocalConfiguration.current.screenHeightDp
                        SeriesGroup(
                            series = element.key,
                            expansions = expansionsMap,
                            colors = colors,
                            screenSize = screenSize,
                            isSheetExpanded = isSheetExpanded,
                            isListView = isListMode,
                            onSetTap = { set -> onSetTap(set) }
                        )
                    }
                }
            }

            if (isSheetExpanded) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .alpha(0.6f)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                )
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
    screenSize: Int,
    isSheetExpanded: Boolean,
    isListView: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = !isSheetExpanded
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
            if (isListView) {
                Column(
                    modifier = Modifier
                        .padding(all = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    expansions.forEach { expansion ->
                        CollectionList(
                            expansion.value,
                            colors,
                            !isSheetExpanded,
                            onSetTap,
                            Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(vertical = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    expansions.forEach { expansion ->
                        val setsCount = expansion.value.size
                        var startIndex = 0
                        var endIndex = 3
                        if (endIndex > setsCount) endIndex = setsCount
                        while (startIndex < setsCount) {
                            CollectionRow(
                                expansion.value.subList(startIndex, endIndex),
                                !isSheetExpanded,
                                screenSize,
                                onSetTap,
                                Modifier.padding(horizontal = 20.dp)
                            )
                            startIndex = endIndex
                            endIndex += 3
                            if (endIndex > setsCount) endIndex = setsCount
                        }
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
    isEnabled: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    sets.forEach { set ->
        val color: Color = colors[set.set] ?: MaterialTheme.colorScheme.primaryContainer
        CollectionCell(set, color, isEnabled, onSetTap)
    }
}

@Composable
fun CollectionCell(
    set: Set,
    color: Color,
    isEnabled: Boolean,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fontColor = PocketBlack

    val backColor = getSimilarColor(
        color = color,
        minSaturation = 0.4f,
        maxSaturation = 0.6f,
        value = 0.9f
    )

    Box(
        modifier = modifier.background(backColor, RoundedCornerShape(10))
            .height(50.dp)
            .clickable(enabled = isEnabled) { onSetTap(set.set) }
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
                    text = set.name.es,
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
                val completion = "%.3f".format(
                    (set.numbers.all.ownedCards.toFloat() / set.numbers.all.totalCards.toFloat()) * 100.0f
                )
                Text(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 6.dp),
                    text = "${completion} %",
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
    isEnabled: Boolean,
    screenSize: Int,
    onSetTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rowHeight = (screenSize.toFloat() / 4.5f).dp
    if (rowHeight < 100.dp) rowHeight = 100.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var i = 0

        sets.forEach { set ->
            SetButton(
                collection = set.set,
                imageURL = set.cover,
                modifier = Modifier
                    .weight(1.0f)
                    .clickable(
                        enabled = isEnabled
                    ) {
                        onSetTap(set.set)
                    }
            )

            i++
        }

        while (i < 3) {
            EmptyBox(
                modifier = Modifier.weight(1.0f)
            )

            i++
        }
    }
}

@Composable
fun SetButton(
    collection: String,
    imageURL: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier.weight(1.0f),
            model = imageURL,
            error = painterResource(R.drawable.booster_placeholder),
            placeholder = painterResource(R.drawable.booster_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Inside
        )
        Text(
            text = collection,
            modifier = Modifier
                .fillMaxHeight(0.2f)
                .padding(bottom = 0.dp)
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

@Composable
fun InfoSetSheet(
    sets: Map<String, List<Set>>,
    activeSets: List<String>,
    rarities: List<String>,
    onSetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 300.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        sets.forEach{ series ->
            items(count = series.value.size) { index ->
                val set = series.value[index]
                if (!set.set.contains("P-")) {
                    InfoSetElement(
                        set = set,
                        isActive = activeSets.contains(set.set),
                        rarities = rarities,
                        onSetClick = onSetClick
                    )
                }
            }
        }
    }
}

@Composable
fun InfoSetElement(
    set: Set,
    isActive: Boolean,
    rarities: List<String>,
    onSetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val setColor = getSimilarColor(
        color = set.color,
        minSaturation = 0.4f,
        maxSaturation = 0.6f,
        value = 0.9f
    )

    val fontColor = MaterialTheme.colorScheme.tertiaryContainer

    val checkColors = CheckboxColors(
        checkedCheckmarkColor = setColor,
        uncheckedCheckmarkColor = setColor,
        checkedBoxColor = MaterialTheme.colorScheme.tertiaryContainer,
        uncheckedBoxColor = setColor,
        disabledCheckedBoxColor = setColor,
        disabledUncheckedBoxColor = setColor,
        disabledIndeterminateBoxColor = setColor,
        checkedBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        uncheckedBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledBorderColor = setColor,
        disabledUncheckedBorderColor = setColor,
        disabledIndeterminateBorderColor = setColor
    )

    var probableBooster = SetsData.getMostProbableBooster(set.set, rarities)
    if (probableBooster == null) {
        probableBooster = Pair(OriginsData.getOriginByID(set.origins[0])!!, 0.0f)
    }

    val greyStrength = if (isActive) 1.0f else 0.5f
    val alpha = if (isActive) 1.0f else 0.5f


    Box(
        modifier = modifier.greyScale(greyStrength)
            .alpha(alpha)
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
                .background(setColor, RoundedCornerShape(5.dp)),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .offset(y = 4.dp)
            ) {
                Text(
                    text = "${set.name.es} (${set.set})",
                    fontSize = 18.sp,
                    color = fontColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                var isFilterActive: Boolean by rememberSaveable {
                    mutableStateOf(isActive)
                }

                Checkbox(
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(0.75f),
                    checked = isFilterActive,
                    colors = checkColors,
                    onCheckedChange = { checkState ->
                        if (checkState) {
                            FiltersManager.addFilter(set.set)
                        } else {
                            FiltersManager.removeFilter(set.set)
                        }
                        onSetClick()
                        isFilterActive = checkState
                    }
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .align(Alignment.CenterEnd)
                            .offset(y = (-4).dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart)
                                .absolutePadding(left = 15.dp),
                            text = probableBooster.first.name.es,
                            fontSize = 16.sp,
                            color = fontColor
                        )
                        val percentage = "%.3f".format(probableBooster.second)
                        Text(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            text = "${percentage}%",
                            fontSize = 16.sp,
                            color = fontColor
                        )
                    }
                }
            }
        }
    }
}