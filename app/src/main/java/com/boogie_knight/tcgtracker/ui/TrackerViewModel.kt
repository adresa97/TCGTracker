package com.boogie_knight.tcgtracker.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.Card
import com.boogie_knight.tcgtracker.models.Game
import com.boogie_knight.tcgtracker.models.GameIcon
import com.boogie_knight.tcgtracker.services.CardsData
import com.boogie_knight.tcgtracker.services.Concepts
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.models.OwnedData
import com.boogie_knight.tcgtracker.services.OriginsData
import com.boogie_knight.tcgtracker.models.Set
import com.boogie_knight.tcgtracker.repositories.SQLiteHandler
import com.boogie_knight.tcgtracker.services.SetsData
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.services.GamesData
import com.boogie_knight.tcgtracker.services.UserOptions
import com.boogie_knight.tcgtracker.ui.screens.binder.FiltersManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

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

            AssetsRepository.init(context)
            UserRepository.init(context)

            GamesData.loadGamesData()

            Concepts.loadJSONData()
            CardsData.loadImageJSONData()
            OriginsData.loadJSONData()
            SetsData.loadJSONData()
            FiltersManager.initiateFilters()
            UserOptions.initUserOptions()

            endLoading()
        }
    }
    //endregion

    //region Functions to get data from objects outside viewModel
    fun getGamesList(): List<String> {
        return GamesData.getGamesList()
    }

    fun getGameIconsMap(): Map<String, GameIcon> {
        return GamesData.getGameIcons()
    }

    fun getGameName(gameCode: String): String {
        return GamesData.getGameName(gameCode)
    }

    fun isGameInDatabase(gameCode: String): Boolean {
        return GamesData.isGameInData(gameCode)
    }

    fun getStartScreenMap(): Map<String, String> {
        return UserOptions.getStartScreenMap()
    }

    fun getUserStartScreen(): Pair<String, String> {
        val userOptions = UserRepository.getAllStartingOptions()
        return userOptions
    }

    fun saveStartScreen(startScreen: String) {
        UserRepository.saveStartScreen(startScreen)
    }

    fun saveLastOpened(lastOpened: String) {
        UserRepository.saveLastOpened(lastOpened)
    }

    fun getPrettyCardsList(game: String, set: String): List<Card> {
        return CardsData.getCardList(
            game = game,
            set = set
        ).map{ card ->
            Card(
                id = card.id,
                name = card.name,
                type = Concepts.getTypeUrl(game, card.type),
                origins = card.origins.map { origin ->
                    OriginsData.getOriginName(game, origin)
                },
                rarity = Concepts.getPrettyRarity(game, card.rarity),
                image = card.image,
                owned = card.owned,
                extra = card.extra,
                firstPrint = card.firstPrint
            )
        }
    }

    fun getRawCardList(game: String, set: String): List<Card> {
        return CardsData.getCardList(
            game = game,
            set = set
        )
    }

    fun getSeriesMap(game: String): Map<String, List<Set>> {
        return SetsData.getSeriesMap(game)
    }

    fun getSetNameFromID(game: String, id: String): String {
        return SetsData.getSetName(game, id)
    }

    fun getSetColors(game: String): Map<String, Color> {
        return SetsData.getSetColors(game)
    }

    fun getSetColorFromID(game: String, id: String): Color? {
        return SetsData.getSetColor(game, id)
    }

    fun isSetPromo(game: String, id: String): Boolean {
        val set = SetsData.getSetFromID(game, id)
        set?.origins?.forEach{ origin ->
            if (OriginsData.isOriginPromo(origin)) return true
        }
        return false
    }

    fun isSetSpecial(game: String, id: String): Boolean {
        val set = SetsData.getSetFromID(game, id)
        set?.origins?.forEach{ origin ->
            if (OriginsData.isOriginSpecial(origin)) return true
        }
        return false
    }

    fun getPrettyRaritiesOwnedData(
        game: String,
        setID: String,
        filters: List<String>
    ): Map<String, OwnedData> {
        if (filters.isEmpty()) return mapOf()

        val set = SetsData.getSetFromID(game, setID)
        if (set == null) return mapOf()

        val outputMap = mutableMapOf<String, OwnedData>()
        set.numbers.byRarity?.forEach{ rarity ->
            if (filters.contains(rarity.key)) {
                val prettyRarity = Concepts.getPrettyRarity(game = game, rarity = rarity.key)
                outputMap.put(prettyRarity, rarity.value)
            }
        }
        return outputMap
    }

    fun getBoostersWithProbabilities(
        game: String,
        set: String,
        rarities: List<String>
    ): Map<Origin, List<Float>> {
        val boostersIDs = SetsData.getSetFromID(game, set)?.origins
        val boosters = mutableListOf<Origin>()
        boostersIDs?.forEach{ id ->
            val origin = OriginsData.getOriginByID(game, id)
            if (origin != null) boosters.add(origin)
        }
        if (boosters.isEmpty()) return mapOf()

        val outputMap = mutableMapOf<Origin, List<Float>>()
        boosters.forEach { booster ->
            val probabilities = SetsData.getBoosterRemainingOdds(set, game, booster, rarities)
            outputMap.put(booster, probabilities)
        }

        return outputMap
    }

    fun getMostProbableBoosterFromList(
        boosters: Map<Origin, List<Float>>
    ): Pair<Origin, Float>? {
        var probableOrigin: Pair<Origin,Float>? = null
        boosters.forEach{ booster ->
            var totalProbability = 1.0f
            for (i in 0 until booster.value.size) {
                totalProbability *= (1.0f - booster.value[i])
            }
            totalProbability = (1.0f - totalProbability) * 100.0f
            if (probableOrigin == null || probableOrigin.second < totalProbability) {
                probableOrigin = Pair(booster.key, totalProbability)
            }
        }
        return probableOrigin
    }

    fun getMostProbableSet(game: String, filter: List<String> = listOf()): Pair<Set, Origin>? {
        return SetsData.getMostProbableSet(game, filter)
    }

    fun getMostProbableSetFromList(
        game: String,
        sets: List<String>,
        filter: List<String> = listOf()
    ): Pair<Set, Origin>? {
        val setList = mutableListOf<Set>()
        sets.forEach{ id ->
            val set = SetsData.getSetFromID(game, id)
            if (set != null) setList.add(set)
        }

        return SetsData.getMostProbableSetFromList(game, setList, filter)
    }

    fun getMostProbableBoosterFromSet(
        game: String,
        set: String,
        filter: List<String> = listOf()
    ): Pair<Origin, Float>? {
        return SetsData.getMostProbableBooster(game, set, filter)
    }

    fun getOriginsColorMap(game: String): Map<String, Color> {
        return OriginsData.getOriginsNameColorMap(game)
    }

    fun isOriginIdBooster(id: String): Boolean {
        if (id == "") return false
        return !OriginsData.isOriginPromo(id)
    }
    //endregion

    //region Functions to change data from objects outside viewModel
    fun changeOwnedCardState(game: String, set: String, cardIndex: Int) {
        viewModelScope.launch {
            startLoading()
            CardsData.changeCardState(game, set, cardIndex)
            val cardList = getRawCardList(game, set)
            SetsData.recalculateSetData(game, cardList, set)
            endLoading()
        }
    }

    fun reloadOwnedCardState(game: String, sets: List<String>) {
        viewModelScope.launch {
            startLoading()
            CardsData.reloadUserData(game, sets)
            sets.forEach { set ->
                val cardList = getRawCardList(game, set)
                SetsData.recalculateSetData(game, cardList, set)
            }
            endLoading()
        }
    }
    //endregion
}