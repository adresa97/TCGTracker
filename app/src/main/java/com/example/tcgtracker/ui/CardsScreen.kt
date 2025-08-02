package com.example.tcgtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tcgtracker.R
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.utils.greyScale

@Composable
fun CardsScreen(
    cardList: List<Card>,
    colors: Map<String, Color>,
    isListMode: Boolean,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isListMode) {
        CardListView(
            cardList = cardList,
            colors = colors,
            onCardTap = onCardTap,
            modifier = modifier
        )
    } else {
        CardGridView(
            cardList = cardList,
            onCardTap = onCardTap,
            modifier = modifier
        )
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
                color = color,
                isOwned = cardOwnership,
                onCardTap = {
                    onCardTap(index)
                    cardOwnership = !cardOwnership
                },
                modifier.absolutePadding(
                    top = if (index == 0) 20.dp else 0.dp,
                    bottom = if (index == cardList.count() - 1) 20.dp else 0.dp
                )
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
            text = id,
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
    color: Color,
    isOwned: Boolean,
    onCardTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bulletColor = PocketBlack
    val fontColor = PocketBlack
    Box(
        modifier = modifier.background(color)
            .height(50.dp)
            .alpha(0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                modifier = Modifier,
                selected = isOwned,
                colors = RadioButtonColors(
                    selectedColor = bulletColor,
                    unselectedColor = bulletColor,
                    disabledSelectedColor = bulletColor,
                    disabledUnselectedColor = bulletColor,
                ),
                onClick = onCardTap
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = id,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = fontColor
                    )
                    Text(
                        text = name,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = fontColor
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rarity,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        color = fontColor
                    )
                    Text(
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
}