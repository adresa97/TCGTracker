package com.boogie_knight.tcgtracker.services

import androidx.compose.ui.graphics.Color
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonOrigin
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.utils.ParseJSON

const val ASSETS_ORIGINS_DATA_FILE_PATH = "PTCGPocket/origins.json"

object OriginsData {
    private val originsData = mutableListOf<Origin>()

    fun loadJSONData() {
        val jsonString = AssetsRepository.getData(ASSETS_ORIGINS_DATA_FILE_PATH)
        val jsonData = ParseJSON(jsonString, Array<JsonOrigin>::class.java)?.asList()
            ?.map{ origin ->
                val color = Color(origin.color[0]/255, origin.color[1]/255, origin.color[2]/255)
                Origin(
                    id = origin.id,
                    name = origin.name,
                    type = origin.type,
                    color = color,
                    cardCount = origin.cardCount,
                    packSize = origin.packSize ?: 0,
                    odds = origin.odds
                )
            } ?: listOf()
        originsData.addAll(jsonData)
    }

    fun getBoostersList(): List<Origin> {
        return originsData.filter{ origin -> origin.type == "BOOSTER" }
    }

    fun getPromoOriginsList(): List<Origin> {
        return originsData.filter{ origin -> origin.type == "PROMO" }
    }

    fun getOriginsID(): List<String> {
        return originsData.map{ origin -> origin.id }
    }

    fun getOriginID(name: String): String {
        val origins = originsData.filter{ origin -> origin.name.es == name }
        if (origins.isEmpty()) return ""
        return origins[0].id
    }

    fun getOriginsName(): List<String> {
        return originsData.map{ origin -> origin.name.es }
    }

    fun getOriginName(id: String): String {
        val origins = originsData.filter{ origin -> origin.id == id }
        if (origins.isEmpty()) return ""
        return origins[0].name.es
    }

    fun getOriginsNameColorMap(): Map<String, Color> {
        val outputMap = mutableMapOf<String, Color>()
        originsData.forEach{ origin ->
            outputMap.put(origin.name.es, origin.color)
        }
        return outputMap
    }

    fun getOriginColor(booster:String): Color? {
        val byName = originsData.find{ origin -> origin.name.es == booster }?.color
        if (byName != null) return byName
        return originsData.find{ origin -> origin.id == booster }?.color
    }

    fun getOriginCardCount(booster: String): Int {
        val byName = originsData.find{ origin -> origin.name.es == booster }?.cardCount ?: 0
        if (byName != 0) return byName
        return originsData.find{ origin -> origin.id == booster }?.cardCount ?: 0
    }

    fun getBoosterOdds(booster: String): Map<String, List<Float>> {
        var origin = originsData.find{ origin -> origin.name.es == booster }
        if (origin == null) origin = originsData.find{ origin -> origin.id == booster }
        if (origin == null) return mapOf()
        if (origin.type != "BOOSTER") return mapOf()
        return origin.odds ?: mapOf()
    }

    fun getOriginType(value: String): String {
        val byName = originsData.find{ origin -> origin.name.es == value }?.type ?: ""
        if (byName.isNotEmpty()) return byName
        return originsData.find{ origin -> origin.id == value }?.type ?: ""
    }

    fun getOriginByID(id: String): Origin? {
        return originsData.find{ origin -> origin.id == id }
    }

    fun isOriginPromo(id: String): Boolean {
        return originsData.find{ origin -> origin.id == id }?.type == "PROMO"
    }
}