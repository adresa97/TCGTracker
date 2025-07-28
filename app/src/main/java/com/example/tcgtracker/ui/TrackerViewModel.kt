package com.example.tcgtracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.tcgtracker.CollectionsData
import com.example.tcgtracker.OwnedCardsData
import com.example.tcgtracker.TCGDexService
import com.example.tcgtracker.models.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackerUIState(
    val collectionsData: CollectionsData = CollectionsData(),
    val ownedCardsData: OwnedCardsData = OwnedCardsData(),
    val selectedSet: String = ""
)

class TrackerViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(TrackerUIState())
    val uiState: StateFlow<TrackerUIState> = _uiState.asStateFlow()

    fun loadCollectionsJSON(applicationContext: Context, jsonPath: String) {
        _uiState.value.collectionsData.loadJSONData(applicationContext, jsonPath)
    }

    fun setCurrentSet(currentSet: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSet = currentSet)
        }
    }

    fun updateJSONSData() {
        _uiState.value.ownedCardsData.updateJSONSData()
    }

    fun getCardsList(context: Context): List<Card> {
        return _uiState.value.ownedCardsData.getCardList(context, _uiState.value.selectedSet)
    }

    fun changeOwnedCardState(set: String, cardIndex: Int) {
        _uiState.value.ownedCardsData.changeCardState(set, cardIndex)
    }
}
