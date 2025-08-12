package com.example.tcgtracker.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tcgtracker.models.CardsData
import com.example.tcgtracker.models.Concepts
import com.example.tcgtracker.models.SQLiteHandler
import com.example.tcgtracker.models.OriginsData
import com.example.tcgtracker.models.SetsData
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.Origin
import com.example.tcgtracker.models.Set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackerUIState(
    val isLoading: Boolean = false,
    val isError: Boolean? = null,
    val isListMode: Boolean = false
)

class TrackerViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(TrackerUIState())
    val uiState: StateFlow<TrackerUIState> = _uiState.asStateFlow()

    fun changeViewMode() {
        _uiState.update { currentState ->
            currentState.copy(isListMode = !uiState.value.isListMode)
        }
    }

    fun startLoading() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
        }
    }

    fun endLoading() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = false)
        }
    }

    // Functions to get data from objects outside viewModel
    fun getPrettyCardsList(context: Context, handler: SQLiteHandler, set: String): List<Card> {
        return CardsData.getCardList(
            applicationContext = context,
            handler = handler,
            set = set
        ).map{ card ->
            Card(
                id = card.id,
                name = card.name,
                type = Concepts.getTypeUrl(card.type),
                origins = card.origins.map { origin ->
                    OriginsData.getOriginName(origin)
                },
                rarity = Concepts.getPrettyRarity(card.rarity),
                image = card.image,
                owned = card.owned,
                baby = card.baby
            )
        }
    }

    fun getRawCardList(context: Context, handler: SQLiteHandler, set: String): List<Card> {
        return CardsData.getCardList(
            applicationContext = context,
            handler = handler,
            set = set
        )
    }

    fun getSeriesMap(): Map<String, List<Set>> {
        return SetsData.getSeriesMap()
    }

    fun getSetNameFromID(id: String): String {
        return SetsData.getSetName(id)
    }

    fun getSetColors(): Map<String, Color> {
        return SetsData.getSetColors()
    }

    fun getSetColorFromID(id: String): Color? {
        return SetsData.getSetColor(id)
    }

    fun getMostProbableSet(filter: List<String> = listOf()): Pair<Set, Origin>? {
        return SetsData.getMostProbableSet(filter)
    }

    fun getMostProbableBoosterFromSet(
        set: String,
        filter: List<String> = listOf()
    ): Pair<Origin, Float>? {
        return SetsData.getMostProbableBooster(set, filter)
    }

    fun getOriginsColorMap(): Map<String, Color> {
        return OriginsData.getOriginsNameColorMap()
    }

    // Functions to change data from objects outside viewModel
    fun changeOwnedCardState(context: Context, handler: SQLiteHandler, set: String, cardIndex: Int) {
        viewModelScope.launch {
            startLoading()
            CardsData.changeCardState(handler, set, cardIndex)
            val cardList = getRawCardList(context, handler, set)
            SetsData.recalculateSetData(handler, cardList, set)
            endLoading()
        }
    }

    fun reloadOwnedCardState(context: Context, handler: SQLiteHandler, sets: List<String>) {
        viewModelScope.launch {
            startLoading()
            CardsData.reloadUserData(handler, sets)
            sets.forEach { set ->
                val cardList = getRawCardList(context, handler, set)
                SetsData.recalculateSetData(handler, cardList, set)
            }
            endLoading()
        }
    }
}