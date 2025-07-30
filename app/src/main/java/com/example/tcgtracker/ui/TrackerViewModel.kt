package com.example.tcgtracker.ui

import android.app.Fragment
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.tcgtracker.SetsData
import com.example.tcgtracker.CardsData
import com.example.tcgtracker.models.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackerUIState(
    val setsData: SetsData = SetsData(),
    val cardsData: CardsData = CardsData(),
    val selectedSet: String = ""
)

class TrackerViewModel() : ViewModel(), DefaultLifecycleObserver {
    private val _uiState = MutableStateFlow(TrackerUIState())
    val uiState: StateFlow<TrackerUIState> = _uiState.asStateFlow()

    fun loadCollectionsJSON(applicationContext: Context) {
        _uiState.value.setsData.loadJSONData(
            applicationContext = applicationContext,
            cardsData = _uiState.value.cardsData
        )
    }

    fun setCurrentSet(currentSet: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedSet = currentSet)
        }
    }

    fun getCardsList(context: Context): List<Card> {
        return _uiState.value.cardsData.getCardList(
            applicationContext = context,
            set = _uiState.value.selectedSet
        )
    }

    fun changeOwnedCardState(context: Context, set: String, cardIndex: Int) {
        _uiState.value.cardsData.changeCardState(set, cardIndex)
        val cardList = getCardsList(context)
        _uiState.value.setsData.recalculateSetData(cardList, set)
    }

    override fun onPause(owner: LifecycleOwner) {
        updateJSONSData()
    }

    fun updateJSONSData() {
        _uiState.value.cardsData.updateJSONSData()
        _uiState.value.setsData.updateUserJSON()
    }
}