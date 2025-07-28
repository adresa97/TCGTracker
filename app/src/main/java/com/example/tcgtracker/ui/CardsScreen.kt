package com.example.tcgtracker.ui

import android.content.Context
import androidx.compose.foundation.clickable
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
import com.example.tcgtracker.R
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.utils.greyScale

@Composable
fun CardsScreen(context: Context, viewModel: TrackerViewModel, set: String, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        modifier = modifier.padding(horizontal = 20.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val cardList = viewModel.uiState.value.getCardsList(context, set)
        cardList.forEach { card ->
            item {
                CardElement(
                    modifier = Modifier.clickable {
                        viewModel.changeOwnedCardState(set, getCardIndex(cardList, card))
                    },
                    cardData = card
                )
            }
        }
    }
}

@Composable
fun CardElement(cardData: Card, modifier: Modifier = Modifier)
{
    Column(
        modifier = modifier.padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            modifier = if(cardData.owned) Modifier else Modifier.greyScale(),
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

fun getCardIndex(cardList: List<Card>, card: Card): Int {
    return cardList.indexOf(card)
}