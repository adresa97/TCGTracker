package com.boogie_knight.tcgtracker.services

import androidx.compose.ui.graphics.Color
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonOrigin
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.utils.ParseJSON

object OriginsData {
    private val originsDataMap = mutableMapOf<String, List<Origin>>()
    private var activeOriginsData = ""

    fun loadJSONData() {
        for (game in Routes.Game.entries)
        {
            val jsonString = AssetsRepository.getData(Routes.getRoute(game, Routes.File.ORIGINS))
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
            originsDataMap.put(game.route, jsonData)
        }
    }

    fun setActiveOrigins(game: Routes.Game) {
        activeOriginsData = game.route
    }

    private fun getActiveOrigins(): List<Origin> {
        return originsDataMap[activeOriginsData] ?: emptyList()
    }

    fun getBoostersList(): List<Origin> {
        return getActiveOrigins().filter{ origin -> origin.type == "BOOSTER" }
    }

    fun getPromoOriginsList(): List<Origin> {
        return getActiveOrigins().filter{ origin -> origin.type == "PROMO" }
    }

    fun getSpecialBoostersList(): List<Origin> {
        return getActiveOrigins().filter{ origin -> origin.type == "SPECIAL" }
    }

    fun getOriginsID(): List<String> {
        return getActiveOrigins().map{ origin -> origin.id }
    }

    fun getOriginID(name: String): String {
        val origins = getActiveOrigins().filter{ origin -> origin.name.es == name }
        if (origins.isEmpty()) return ""
        return origins[0].id
    }

    fun getOriginsName(): List<String> {
        return getActiveOrigins().map{ origin -> origin.name.es }
    }

    fun getOriginName(id: String): String {
        val origins = getActiveOrigins().filter{ origin -> origin.id == id }
        if (origins.isEmpty()) return ""
        return origins[0].name.es
    }

    fun getOriginsNameColorMap(): Map<String, Color> {
        val outputMap = mutableMapOf<String, Color>()
        getActiveOrigins().forEach{ origin ->
            outputMap.put(origin.name.es, origin.color)
        }
        return outputMap
    }

    fun getOriginColor(booster:String): Color? {
        val byName = getActiveOrigins().find{ origin -> origin.name.es == booster }?.color
        if (byName != null) return byName
        return getActiveOrigins().find{ origin -> origin.id == booster }?.color
    }

    fun getOriginCardCount(booster: String): Int {
        val byName = getActiveOrigins().find{ origin -> origin.name.es == booster }?.cardCount ?: 0
        if (byName != 0) return byName
        return getActiveOrigins().find{ origin -> origin.id == booster }?.cardCount ?: 0
    }

    fun getBoosterOdds(booster: String): Map<String, List<Float>> {
        var origin = getActiveOrigins().find{ origin -> origin.name.es == booster }
        if (origin == null) origin = getActiveOrigins().find{ origin -> origin.id == booster }
        if (origin == null) return mapOf()
        if (origin.type != "BOOSTER") return mapOf()
        return origin.odds ?: mapOf()
    }

    fun getOriginType(value: String): String {
        val byName = getActiveOrigins().find{ origin -> origin.name.es == value }?.type ?: ""
        if (byName.isNotEmpty()) return byName
        return getActiveOrigins().find{ origin -> origin.id == value }?.type ?: ""
    }

    fun getOriginByID(id: String): Origin? {
        return getActiveOrigins().find{ origin -> origin.id == id }
    }

    fun getAllOrigins(): List<Origin> {
        val origins = mutableListOf<Origin>()
        originsDataMap.forEach{ originsData ->
            origins.addAll(originsData.value)
        }
        return origins
    }

    fun isOriginBooster(id: String): Boolean {
        return getAllOrigins().find{ origin -> origin.id == id }?.type == "BOOSTER"
    }

    fun isOriginPromo(id: String): Boolean {
        return getAllOrigins().find{ origin -> origin.id == id }?.type == "PROMO"
    }

    fun isOriginSpecial(id: String): Boolean {
        return getAllOrigins().find{ origin -> origin.id == id }?.type == "SPECIAL"
    }
}