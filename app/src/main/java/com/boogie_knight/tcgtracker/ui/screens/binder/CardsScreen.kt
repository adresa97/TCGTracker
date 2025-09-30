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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.boogie_knight.tcgtracker.R
import com.boogie_knight.tcgtracker.models.Card
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.models.OwnedData
import com.boogie_knight.tcgtracker.services.SetsData
import com.boogie_knight.tcgtracker.ui.TrackerViewModel
import com.boogie_knight.tcgtracker.ui.theme.PocketBlack
import com.boogie_knight.tcgtracker.ui.theme.getSimilarColor
import com.boogie_knight.tcgtracker.ui.theme.ptcgFontFamily
import com.boogie_knight.tcgtracker.utils.greyScale
import com.smarttoolfactory.extendedcolors.util.ColorUtil.colorToHSV
import com.smarttoolfactory.extendedcolors.util.HSVUtil.hsvToColorInt
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class CardsScreen(val currentSet: String): NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    currentSet: String,
    onBackTap: () -> Unit,
    onOptionsTap: () -> Unit,
    trackerViewModel: TrackerViewModel = viewModel()
) {
    // Collect uiState
    val trackerUIState by trackerViewModel.uiState.collectAsState()

    // Get isListMode from current state
    var isListMode: Boolean by rememberSaveable {
        mutableStateOf(trackerUIState.isListMode)
    }

    // Get active rarities from FiltersManager
    var currentFilters: List<String> by rememberSaveable {
        mutableStateOf(FiltersManager.getActiveFilters())
    }

    // UI color
    val uiColor = SetsData.getSetColor(currentSet)
        ?: MaterialTheme.colorScheme.primaryContainer

    // Bottom sheet state
    val scaffoldScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var isSheetExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    var isFiltersSheet: Boolean by rememberSaveable { mutableStateOf(false) }

    // Bottom sheet info state
    var isBoosterView: Boolean by rememberSaveable { mutableStateOf(true) }

    // Map of origins and probabilities
    var boostersWithProbabilities by rememberSaveable {
        mutableStateOf(
            value = trackerViewModel.getBoostersWithProbabilities(
                set = currentSet,
                rarities = currentFilters))
    }

    // Probable booster to show on bottom sheet
    var probableBooster by rememberSaveable {
        mutableStateOf(
            value = trackerViewModel.getMostProbableBoosterFromList(
                boosters = boostersWithProbabilities)
        )
    }

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
                    title = { Text(text = SetsData.getSetName(currentSet)) },
                    modifier = Modifier,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onBackTap()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                    },
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
            sheetContent = {
                val originID = probableBooster?.first?.id ?: ""
                val originName =
                    if (trackerViewModel.isOriginIdBooster(originID)) probableBooster?.first?.name?.es ?: ""
                    else ""
                val originColor =
                    if (originName == "") MaterialTheme.colorScheme.surface
                    else probableBooster?.first?.color ?: MaterialTheme.colorScheme.surface
                BottomSheet(
                    title = originName,
                    uiColor = uiColor,
                    trackerColor = originColor,
                    peekArea = bottomBarHeight,
                    safeArea = safeArea,
                    isFiltersSheet = isFiltersSheet,
                    isAlreadyFiltered = !FiltersManager.areAllFiltersActivated(currentFilters),
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
                        currentFilters = FiltersManager.getActiveFilters()
                        boostersWithProbabilities = trackerViewModel.getBoostersWithProbabilities(
                            set = currentSet,
                            rarities = currentFilters
                        )
                        probableBooster = trackerViewModel.getMostProbableBoosterFromList(
                            boosters = boostersWithProbabilities
                        )
                    },
                    infoScreen = {
                        if (isBoosterView) {
                            InfoBoosterSheet(
                                setID = currentSet,
                                boosters = boostersWithProbabilities
                            )
                        } else {
                            InfoRaritySheet(
                                setID = currentSet,
                                rarityCards = trackerViewModel.getPrettyRaritiesOwnedData(
                                    setID = currentSet,
                                    filters = FiltersManager.getFilters().keys.toList()
                                )
                            )
                        }
                    },
                    isInfoLeftScreen = isBoosterView,
                    infoLeftButtonText = if (currentSet.contains("P-")) "" else "Por sobre",
                    onLeftInfoClick = {
                        isBoosterView = true
                    },
                    infoRightButtonText = if (currentSet.contains("P-")) "" else "Por rareza",
                    onRightInfoClick = {
                        isBoosterView = false
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
                val cardList = trackerViewModel.getPrettyCardsList(currentSet)
                val colors = trackerViewModel.getOriginsColorMap()

                val filteredCardList =
                    if (trackerViewModel.isSetPromo(currentSet)) cardList
                    else cardList.filter { card -> FiltersManager.isFilterActivated(card.rarity, true) }
                if (filteredCardList.isEmpty()) {
                    if (cardList.isEmpty()) {
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
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(all = 10.dp),
                            text = "No se encuentran cartas para los filtros activos.",
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        )
                    }
                } else {
                    if (isListMode) {
                        CardListView(
                            cardList = filteredCardList,
                            colors = colors,
                            isSheetExpanded = isSheetExpanded,
                            onCardTap = { index ->
                                trackerViewModel.changeOwnedCardState(
                                    set = currentSet,
                                    cardIndex = index
                                )
                                boostersWithProbabilities = trackerViewModel.getBoostersWithProbabilities(
                                    set = currentSet,
                                    rarities = currentFilters
                                )
                                probableBooster = trackerViewModel.getMostProbableBoosterFromList(
                                    boosters = boostersWithProbabilities
                                )
                            }
                        )
                    } else {
                        CardGridView(
                            cardList = filteredCardList,
                            isSheetExpanded = isSheetExpanded,
                            onCardTap = { index ->
                                trackerViewModel.changeOwnedCardState(
                                    set = currentSet,
                                    cardIndex = index
                                )
                                boostersWithProbabilities = trackerViewModel.getBoostersWithProbabilities(
                                    set = currentSet,
                                    rarities = currentFilters
                                )
                                probableBooster = trackerViewModel.getMostProbableBoosterFromList(
                                    boosters = boostersWithProbabilities
                                )
                            }
                        )
                    }
                }
            }

            if (isSheetExpanded) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .alpha(0.6f)
                        .background(PocketBlack)
                )
            }
        }
    }
}

@Composable
fun CardGridView(
    cardList: List<Card>,
    isSheetExpanded: Boolean,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 20.dp),
        columns = GridCells.Adaptive(100.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy((-10).dp),
        userScrollEnabled = !isSheetExpanded
    ) {
        items(cardList.count()) { index ->
            var cardOwnership: Boolean by rememberSaveable {
                mutableStateOf(cardList[index].owned)
            }
            CardImage(
                id = cardList[index].id,
                isOwned = cardOwnership,
                image = cardList[index].image,
                modifier = Modifier.clickable(enabled = !isSheetExpanded) {
                    onCardTap(index)
                    cardOwnership = !cardOwnership
                }
            )
        }
    }
}

@Composable
fun CardListView(
    cardList: List<Card>,
    colors: Map<String, Color>,
    isSheetExpanded: Boolean,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        userScrollEnabled = !isSheetExpanded
    ) {
        items(cardList.count()) { index ->
            var cardOwnership: Boolean by rememberSaveable {
                mutableStateOf(cardList[index].owned)
            }

            val boosters = cardList[index].origins
            val booster = when (boosters.count()) {
                0 -> ""
                1 -> {
                    if (boosters[0] == "") {
                        "Desbloqueable"
                    } else {
                        boosters[0]
                    }
                }
                else -> "Todos"
            }
            val color = when (booster) {
                "" -> MaterialTheme.colorScheme.primaryContainer
                "Desbloqueable" -> Color(0xFFCCCCCC)
                "Todos" -> Color(0xFFefefef)
                else -> {
                    val rawColor = colors[booster] ?: MaterialTheme.colorScheme.primaryContainer
                    val hsv = colorToHSV(rawColor)
                    if (hsv[1] < 0.4f) hsv[1] = 0.4f
                    else if (hsv[1] > 0.6f) hsv[1] = 0.6f
                    hsv[2] = 0.9f
                    Color(hsvToColorInt(hsv))
                }
            }

            CardBullet(
                id = cardList[index].id,
                name = cardList[index].name.es,
                booster = booster,
                rarity = cardList[index].rarity,
                type = cardList[index].type,
                color = color,
                isOwned = cardOwnership,
                isEnabled = !isSheetExpanded,
                onCardTap = {
                    onCardTap(index)
                    cardOwnership = !cardOwnership
                },
                modifier
                    .absolutePadding (
                        top = if (index == 0) 20.dp else 0.dp,
                        bottom = if (index == cardList.count() - 1) 20.dp else 0.dp
                    )
                    .alpha(0.75f)
            )
        }
    }
}

@Composable
fun CardImage(
    id: String,
    isOwned: Boolean,
    image: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            modifier = if (isOwned) Modifier else Modifier.greyScale(0.1f).alpha(0.5f),
            model = image,
            error = painterResource(R.drawable.card_back),
            placeholder = painterResource(R.drawable.card_back),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            text = id.substringAfterLast('-'),
            modifier = Modifier.padding(bottom = 0.dp)
        )
    }
}

@Composable
fun CardBullet(
    id: String,
    name: String,
    booster: String,
    rarity: String,
    type: String,
    color: Color,
    isOwned: Boolean,
    isEnabled: Boolean,
    onCardTap: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val numeral = id.substringAfterLast('-')
    val fontColor = MaterialTheme.colorScheme.tertiaryContainer
    val checkColor = CheckboxColors(
        checkedCheckmarkColor = color,
        uncheckedCheckmarkColor = color,
        checkedBoxColor = MaterialTheme.colorScheme.tertiaryContainer,
        uncheckedBoxColor = color,
        disabledCheckedBoxColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledUncheckedBoxColor = color,
        disabledIndeterminateBoxColor = color,
        checkedBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        uncheckedBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledUncheckedBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
        disabledIndeterminateBorderColor = MaterialTheme.colorScheme.tertiaryContainer
    )
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(10))
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .alpha(1.0f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                modifier = Modifier.scale(1.5f).offset(x = (-5).dp),
                checked = isOwned,
                colors = checkColor,
                enabled = isEnabled,
                onCheckedChange = onCardTap
            )
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)
                    .absolutePadding(right = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top left
                Text(
                    modifier = Modifier.align(Alignment.TopStart).offset(y = (-5).dp),
                    text = name,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = fontColor
                )
                // Top right
                Text(
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-5).dp),
                    text = type,
                    textAlign = TextAlign.Left,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontFamily = ptcgFontFamily,
                    color = fontColor
                )
                // Bottom left
                Text(
                    modifier = Modifier.align(Alignment.BottomStart).offset(y = 6.dp),
                    text = "${numeral} ${rarity}",
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                    fontFamily = FontFamily.Monospace,
                    color = fontColor
                )
                // Bottom right
                Text(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 6.dp),
                    text = booster,
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
fun InfoBoosterSheet(
    setID: String,
    boosters: Map<Origin, List<Float>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (!setID.contains("P-")) {
            if (boosters.isNotEmpty()) {
                boosters.forEach{ origin ->
                    item {
                        InfoBoosterElement(
                            booster = origin.key,
                            probabilities = origin.value
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "No se encontraron sobres en ${setID}",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "${setID} es una colección promocional",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InfoBoosterElement(
    booster: Origin,
    probabilities: List<Float>,
    modifier: Modifier = Modifier
) {
    val boosterColor = getSimilarColor(
        color = booster.color,
        minSaturation = 0.4f,
        maxSaturation = 0.6f,
        value = 0.9f
    )

    val fontColor = MaterialTheme.colorScheme.tertiaryContainer

    val cardOdds = mutableListOf<String>()
    var totalOddCounter = 1.0f
    for (i in 0 until probabilities.size) {
        cardOdds.add("%.1f".format(probabilities[i] * 100.0f))
        totalOddCounter *= (1 - probabilities[i])
    }
    val totalOdd = "%.3f".format(
        (1 - totalOddCounter) * 100.0f
    )

    Column(
        modifier = modifier.fillMaxWidth()
            .background(boosterColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .offset(y = 4.dp)
        ) {
            Text(
                text = booster.name.es,
                fontSize = 18.sp,
                color = fontColor
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .offset(y = (-4).dp)
        ) {
            // Each card odds
            Row(
                modifier = Modifier.align(Alignment.CenterStart)
                    .absolutePadding(left = 15.dp)
                    .fillMaxWidth(0.7f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until cardOdds.size) {
                    Text(
                        text = cardOdds[i],
                        textAlign = TextAlign.Left,
                        fontSize = 14.sp,
                        color = fontColor
                    )
                }
            }

            // Total odd
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = "${totalOdd}%",
                fontSize = 18.sp,
                color = fontColor
            )
        }
    }
}

@Composable
fun InfoRaritySheet(
    setID: String,
    rarityCards: Map<String, OwnedData>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (!setID.contains("P-")) {
            if (rarityCards.isNotEmpty()) {
                val entryList = rarityCards.entries.toList()
                items(rarityCards.size){ index ->
                    val rarity = entryList[index]
                    InfoRarityElement(
                        rarity = rarity.key,
                        total = rarity.value.totalCards,
                        owned = rarity.value.ownedCards
                    )

                    if (index != rarityCards.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 3.dp)
                                .alpha(0.3f),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "No se encontraron rarezas en ${setID}",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "${setID} es una colección promocional",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InfoRarityElement(
    rarity: String,
    total: Int,
    owned: Int,
    modifier: Modifier = Modifier
) {
    val fontColor = MaterialTheme.colorScheme.onTertiaryContainer

    Row(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rarity,
            fontSize = 18.sp,
            color = fontColor
        )

        Text(
            text = "${owned} / ${total}",
            fontSize = 18.sp,
            color = fontColor
        )
    }
}