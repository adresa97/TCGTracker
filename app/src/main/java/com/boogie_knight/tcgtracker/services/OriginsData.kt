package com.boogie_knight.tcgtracker.services

import androidx.compose.ui.graphics.Color
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonOrigin
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.utils.ParseJSON

object OriginsData {
    private val originsDataMap = mutableMapOf<String, List<Origin>>()

    fun loadJSONData() {
        val games = GamesData.getGamesList()
        for (game in games)
        {
            val jsonString = AssetsRepository.getData(GamesData.getGameOriginsRoute(game))
            val jsonData = ParseJSON(jsonString, Array<JsonOrigin>::class.java)?.asList()
                ?.map{ origin ->
                    val color = Color(origin.color[0]/255, origin.color[1]/255, origin.color[2]/255)
                    Origin(
                        id = origin.id,
                        name = origin.name,
                        type = origin.type,
                        color = color,
                        cardCount = origin.cardCount,
                        packSize = origin.packSize,
                        odds = origin.odds
                    )
                } ?: listOf()
            originsDataMap.put(game, jsonData)
        }
    }

    fun getGlobalList(): List<Origin> {
        val originList = mutableListOf<Origin>()
        originsDataMap.forEach{ origin ->
            originList.addAll(origin.value)
        }
        return originList
    }

    fun getGameList(game: String): List<Origin> {
        val originList = originsDataMap[game]
        if (originList == null) return emptyList()
        return originList
    }

    fun getBoostersList(): List<Origin> {
        return getGlobalList().filter{ origin -> origin.type == "BOOSTER" }
    }

    fun getPromoOriginsList(): List<Origin> {
        return getGlobalList().filter{ origin -> origin.type == "PROMO" }
    }

    fun getSpecialBoostersList(): List<Origin> {
        return getGlobalList().filter{ origin -> origin.type == "SPECIAL" }
    }

    fun getOriginsID(): List<String> {
        return getGlobalList().map{ origin -> origin.id }
    }

    fun getOriginID(name: String): String {
        val origins = getGlobalList().filter{ origin -> origin.name.es == name }
        if (origins.isEmpty()) return ""
        return origins[0].id
    }

    fun getOriginsName(): List<String> {
        return getGlobalList().map{ origin -> origin.name.es }
    }

    fun getOriginName(game: String, id: String): String {
        val origins = getGameList(game).filter{ origin -> origin.id == id }
        if (origins.isEmpty()) return ""
        return origins[0].name.es
    }

    fun getOriginsNameColorMap(game: String): Map<String, Color> {
        val outputMap = mutableMapOf<String, Color>()
        getGameList(game).forEach{ origin ->
            outputMap.put(origin.name.es, origin.color)
        }
        return outputMap
    }

    fun getOriginColor(game: String, booster: String): Color? {
        val byName = getGameList(game).find{ origin -> origin.name.es == booster }?.color
        if (byName != null) return byName
        return getGameList(game).find{ origin -> origin.id == booster }?.color
    }

    fun getOriginCardCount(game: String, booster: String): Int {
        val byName = getGameList(game).find{ origin -> origin.name.es == booster }?.cardCount ?: 0
        if (byName != 0) return byName
        return getGameList(game).find{ origin -> origin.id == booster }?.cardCount ?: 0
    }

    fun getBoosterOdds(game: String, booster: String): Map<String, List<Float>> {
        var origin = getGameList(game).find{ origin -> origin.name.es == booster }
        if (origin == null) origin = getGlobalList().find{ origin -> origin.id == booster }
        if (origin == null) return mapOf()
        if (origin.type != "BOOSTER") return mapOf()
        return origin.odds ?: mapOf()
    }

    fun getOriginType(game: String, value: String): String {
        val byName = getGameList(game).find{ origin -> origin.name.es == value }?.type ?: ""
        if (byName.isNotEmpty()) return byName
        return getGameList(game).find{ origin -> origin.id == value }?.type ?: ""
    }

    fun getOriginByID(game: String, id: String): Origin? {
        return getGameList(game).find{ origin -> origin.id == id }
    }

    fun isOriginBooster(id: String): Boolean {
        return getGlobalList().find{ origin -> origin.id == id }?.type == "BOOSTER"
    }

    fun isOriginPromo(id: String): Boolean {
        return getGlobalList().find{ origin -> origin.id == id }?.type == "PROMO"
    }

    fun isOriginSpecial(id: String): Boolean {
        return getGlobalList().find{ origin -> origin.id == id }?.type == "SPECIAL"
    }
}