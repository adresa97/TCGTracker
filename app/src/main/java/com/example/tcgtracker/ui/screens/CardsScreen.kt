package com.example.tcgtracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import coil3.compose.AsyncImage
import com.example.tcgtracker.R
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.ui.TrackerViewModel
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.PocketWhite
import com.example.tcgtracker.ui.theme.ptcgFontFamily
import com.example.tcgtracker.utils.greyScale
import com.smarttoolfactory.extendedcolors.util.ColorUtil.colorToHSV
import com.smarttoolfactory.extendedcolors.util.HSVUtil.hsvToColorInt
import kotlinx.serialization.Serializable

@Serializable
data class CardsScreen(val currentSet: String): NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    context: Context,
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

    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            // Top bar
            topBar = {
                // Title text
                val title = trackerUIState.setsData.getSetName(currentSet)

                // UI color
                var uiColor = trackerUIState.setsData.getSetColor(currentSet)
                if (uiColor == null) uiColor = MaterialTheme.colorScheme.primaryContainer
                else {
                    val hsv = colorToHSV(uiColor)
                    hsv[2] = 0.5f
                    uiColor = Color(hsvToColorInt(hsv))
                }

                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = uiColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = { Text(title) },
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
            // Bottom bar
            bottomBar = {
                // UI color
                var uiColor = trackerUIState.setsData.getSetColor(currentSet)
                if (uiColor == null) uiColor = MaterialTheme.colorScheme.primaryContainer
                else {
                    val hsv = colorToHSV(uiColor)
                    hsv[2] = 0.5f
                    uiColor = Color(hsvToColorInt(hsv))
                }

                // Get most probable booster out of current set and its color
                val booster = trackerUIState.setsData.getMostProbableBooster(currentSet, trackerUIState.originsData)
                var boosterColor = booster?.first?.color
                if (boosterColor == null) boosterColor = MaterialTheme.colorScheme.surface
                else {
                    val hsv = colorToHSV(boosterColor)
                    hsv[2] = 0.8f
                    boosterColor = Color(hsvToColorInt(hsv))
                }

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
                                .background(boosterColor, RoundedCornerShape(percent = 50))
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(percent = 50),
                                    clip = true,
                                    ambientColor = Color(0.0f, 0.0f, 0.0f, 0.0f),
                                    spotColor = PocketWhite.apply { android.graphics.Color.alpha(100) }
                                )
                                .border(
                                    2.dp,
                                    uiColor.apply { android.graphics.Color.alpha(50) },
                                    RoundedCornerShape(percent = 50)
                                )
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .wrapContentWidth(align = Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = booster?.first?.name ?: "",
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
                val cardList = trackerViewModel.getPrettyCardsList(context, currentSet)
                val colors = trackerUIState.originsData.getOriginsNameColorMap()

                if (isListMode) {
                    CardListView(
                        cardList = cardList,
                        colors = colors,
                        onCardTap = { index ->
                            trackerViewModel.changeOwnedCardState(
                                context = context,
                                set = currentSet,
                                cardIndex = index
                            )
                        }
                    )
                } else {
                    CardGridView(
                        cardList = cardList,
                        onCardTap = { index ->
                            trackerViewModel.changeOwnedCardState(
                                context = context,
                                set = currentSet,
                                cardIndex = index
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardGridView(
    cardList: List<Card>,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 20.dp),
        columns = GridCells.Adaptive(100.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy((-10).dp)
    ) {
        items(cardList.count()) { index ->
            var cardOwnership: Boolean by rememberSaveable {
                mutableStateOf(cardList[index].owned)
            }
            CardImage(
                id = cardList[index].id,
                isOwned = cardOwnership,
                image = cardList[index].image,
                modifier = Modifier.clickable {
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
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
                else -> colors[booster] ?: MaterialTheme.colorScheme.primaryContainer
            }

            CardBullet(
                id = cardList[index].id,
                name = cardList[index].name.es,
                booster = booster,
                rarity = cardList[index].rarity,
                type = cardList[index].type,
                color = color,
                isOwned = cardOwnership,
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
    onCardTap: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val numeral = id.substringAfterLast('-')
    val fontColor = PocketBlack
    val checkColor = CheckboxColors(
        checkedCheckmarkColor = color,
        uncheckedCheckmarkColor = color,
        checkedBoxColor = PocketBlack,
        uncheckedBoxColor = color,
        disabledCheckedBoxColor = color,
        disabledUncheckedBoxColor = color,
        disabledIndeterminateBoxColor = color,
        checkedBorderColor = PocketBlack,
        uncheckedBorderColor = PocketBlack,
        disabledBorderColor = color,
        disabledUncheckedBorderColor = color,
        disabledIndeterminateBorderColor = color
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
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
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