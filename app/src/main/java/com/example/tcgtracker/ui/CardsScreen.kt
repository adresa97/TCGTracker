package com.example.tcgtracker.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.premultipliedAlpha
import com.example.tcgtracker.OwnedCardsData
import com.example.tcgtracker.R
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.rarityMap
import com.example.tcgtracker.ui.theme.PocketBlack
import com.example.tcgtracker.ui.theme.boosterColors
import com.example.tcgtracker.utils.greyScale

@Composable
fun CardsScreen(
    cardList: List<Card>,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    /*
    CardGridView(
        cardList = cardList,
        onCardTap = onCardTap,
        modifier = modifier
    )
     */
    CardListView(
        cardList = cardList,
        onCardTap = onCardTap,
        modifier = modifier
    )
}

@Composable
fun CardGridView(
    cardList: List<Card>,
    onCardTap: (cardIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 20.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
                    cardOwnership = cardList[index].owned
                }
            )
        }
    }
}

@Composable
fun CardListView(
    cardList: List<Card>,
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
            val boosters = cardList[index].booster
            val booster = if (boosters.count() == 0) {
                "Unlockable"
            } else if (boosters.count() > 1) {
                "All"
            } else {
                boosters[0]
            }

            CardBullet(
                id = cardList[index].id,
                name = cardList[index].name,
                booster = booster,
                rarity = cardList[index].rarity,
                isOwned = cardOwnership,
                onCardTap = {
                    onCardTap(index)
                    cardOwnership = cardList[index].owned
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
            modifier = if (isOwned) Modifier else Modifier.greyScale().alpha(0.5f),
            model = image,
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
    isOwned: Boolean,
    onCardTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val boosterColor = boosterColors[booster] ?: MaterialTheme.colorScheme.primaryContainer
    val bulletColor = PocketBlack
    val fontColor = PocketBlack
    Box(
        modifier = modifier.background(boosterColor)
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                modifier = Modifier,
                selected = isOwned,
                colors = RadioButtonColors(
                    bulletColor,
                    bulletColor,
                    bulletColor,
                    bulletColor,
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

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    CardBullet(
        id = "A1-001",
        name = "Bulbasaur",
        booster = "Mewtwo",
        rarity = rarityMap["One Diamond"] ?: "",
        isOwned = true,
        modifier = Modifier.fillMaxWidth()
    )
}