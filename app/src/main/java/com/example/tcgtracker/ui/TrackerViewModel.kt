package com.example.tcgtracker.ui

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tcgtracker.SetsData
import com.example.tcgtracker.CardsData
import com.example.tcgtracker.Concepts
import com.example.tcgtracker.OriginsData
import com.example.tcgtracker.models.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackerUIState(
    val setsData: SetsData = SetsData(),
    val cardsData: CardsData = CardsData(),
    val originsData: OriginsData = OriginsData(),
    val selectedSet: String = "",
    val isListMode: Boolean = false
)

class TrackerViewModel() : ViewModel(), DefaultLifecycleObserver {
    private val _uiState = MutableStateFlow(TrackerUIState())
    val uiState: StateFlow<TrackerUIState> = _uiState.asStateFlow()

    fun loadData(context: Context) {
        Concepts.loadJSONData(context)
        _uiState.value.originsData.loadJSONData(context)
        _uiState.value.setsData.loadJSONData(
            applicationContext = context,
            cardsData = _uiState.value.cardsData
        )
    }

    fun setCurrentSet(currentSet: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSet = currentSet)
        }
    }

    fun changeViewMode() {
        _uiState.update { currentState ->
            currentState.copy(isListMode = !uiState.value.isListMode)
        }
    }

    fun getPrettyCardsList(context: Context): List<Card> {
        return _uiState.value.cardsData.getCardList(
            applicationContext = context,
            set = _uiState.value.selectedSet
        ).map{ card ->
            Card(
                id = card.id,
                name = card.name,
                type = Concepts.getTypeUrl(card.type),
                origins = card.origins.map { origin ->
                    _uiState.value.originsData.getOriginName(origin)
                },
                rarity = Concepts.getPrettyRarity(card.rarity),
                image = card.image,
                owned = card.owned,
                baby = card.baby
            )
        }
    }

    fun getRawCardList(context: Context): List<Card> {
        return _uiState.value.cardsData.getCardList(
            applicationContext = context,
            set = _uiState.value.selectedSet
        )
    }

    fun changeOwnedCardState(context: Context, set: String, cardIndex: Int) {
        _uiState.value.cardsData.changeCardState(set, cardIndex)
        val cardList = getPrettyCardsList(context)
        _uiState.value.setsData.recalculateSetData(cardList, set)
    }

    fun reloadOwnedCardState(context: Context, sets: List<String>) {
        _uiState.value.cardsData.reloadUserJSONSData(context, sets)
        val cardList = getPrettyCardsList(context)
        sets.forEach{ set ->
            _uiState.value.setsData.recalculateSetData(cardList, set)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        updateJSONSData()
    }

    fun updateJSONSData() {
        _uiState.value.cardsData.updateUserJSONSData()
        _uiState.value.setsData.updateUserJSON()
    }
}