package com.example.tcgtracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.utils.greyScale

@Composable
fun CardsGrid(service: TCGDexService, set: String, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 20.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val cardList = service.getCardsList(set)
        cardList.forEach { card ->
            item {
                CardElement(card)
            }
        }
    }
}

@Composable
fun CardElement(cardData: Card, modifier: Modifier = Modifier)
{
    val isNotOwned = true
    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            modifier = if(isNotOwned) Modifier.greyScale() else Modifier,
            model = cardData.image,
            placeholder = painterResource(R.drawable.card_back),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            text = cardData.id,
            modifier = Modifier.padding(bottom = 0.dp)
        )
    }
}