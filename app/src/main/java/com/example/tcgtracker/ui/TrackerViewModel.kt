package com.example.tcgtracker.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tcgtracker.models.CardsData
import com.example.tcgtracker.models.Concepts
import com.example.tcgtracker.models.SQLiteHandler
import com.example.tcgtracker.models.OriginsData
import com.example.tcgtracker.models.SetsData
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.DatabaseHandler
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

    var isLoading = SavedStateHandle(mutableMapOf(Pair("value", uiState.value.isLoading)))

    //region Modify state values
    fun changeViewMode() {
        _uiState.update { currentState ->
            currentState.copy(isListMode = !uiState.value.isListMode)
        }
    }

    fun startLoading() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
        }
        isLoading["value"] = true
    }

    fun endLoading() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = false)
        }
        isLoading["value"] = false
    }

    fun isLoading(): Boolean {
        isLoading["value"] = uiState.value.isLoading
        return isLoading["value"] ?: false
    }
    //endregion

    //region Functions to setup outside objects data
    fun loadData(context: Context) {
        viewModelScope.launch {
            startLoading()
            DatabaseHandler.init(context)
            Concepts.loadJSONData(context)
            OriginsData.loadJSONData(context)
            SetsData.loadJSONData(context)
            endLoading()
        }
    }
    //endregion

    //region Functions to get data from objects outside viewModel
    fun getPrettyCardsList(context: Context, set: String): List<Card> {
        return CardsData.getCardList(
            applicationContext = context,
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

    fun getRawCardList(context: Context, set: String): List<Card> {
        return CardsData.getCardList(
            applicationContext = context,
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
    //endregion

    //region Functions to change data from objects outside viewModel
    fun changeOwnedCardState(context: Context, set: String, cardIndex: Int) {
        viewModelScope.launch {
            startLoading()
            CardsData.changeCardState(set, cardIndex)
            val cardList = getRawCardList(context, set)
            SetsData.recalculateSetData(cardList, set)
            endLoading()
        }
    }

    fun reloadOwnedCardState(context: Context, sets: List<String>) {
        viewModelScope.launch {
            startLoading()
            CardsData.reloadUserData(sets)
            sets.forEach { set ->
                val cardList = getRawCardList(context, set)
                SetsData.recalculateSetData(cardList, set)
            }
            endLoading()
        }
    }
    //endregion
}