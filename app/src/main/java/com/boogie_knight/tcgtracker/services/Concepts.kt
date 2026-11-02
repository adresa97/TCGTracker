package com.boogie_knight.tcgtracker.services

import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonConcepts
import com.boogie_knight.tcgtracker.utils.ParseJSON

object Concepts {
    data class ConceptData (
        val types: Map<String, String>,
        val rarities: Map<String, String>,
        val reverseRarities: Map<String, String>,
        val parallel: Map<String, String>
    )

    private val conceptDataMap = mutableMapOf<String, ConceptData>()
    private var activeConceptData = ""

    fun loadJSONData() {
        for (game in Routes.Game.entries) {
            val jsonString = AssetsRepository.getData(Routes.getRoute(game, Routes.File.CONCEPTS))
            val jsonData = ParseJSON(jsonString, JsonConcepts::class.java)
            if (jsonData != null) {
                val reverseRarities = mutableMapOf<String, String>()
                jsonData.rarities.forEach { rarity ->
                    reverseRarities.put(key = rarity.value, value = rarity.key)
                }
                conceptDataMap.put(game.route, ConceptData(
                    types = jsonData.types,
                    rarities = jsonData.rarities,
                    reverseRarities = reverseRarities,
                    parallel = jsonData.parallel ?: mapOf()
                ))
            }
        }
    }

    fun setActiveConcept(game: Routes.Game) {
        activeConceptData = game.route
    }

    private fun getActiveConcept(): ConceptData? {
        return conceptDataMap[activeConceptData]
    }

    fun getTypes(): List<String> {
        return getActiveConcept()?.types?.keys?.toList() ?: emptyList()
    }
    
    fun getTypeUrls(): List<String> {
        return getActiveConcept()?.types?.values?.toList() ?: emptyList()
    }

    fun getGlobalRarities(): List<String> {
        val rarities = mutableListOf<String>()
        for (data in conceptDataMap)
        {
            rarities.addAll(data.value.rarities.keys.toList())
        }

        return rarities
    }

    fun getRarities(): List<String> {
        return getActiveConcept()?.rarities?.keys?.toList() ?: emptyList()
    }

    fun getPrettyRarities(): List<String> {
        return getActiveConcept()?.rarities?.values?.toList() ?: emptyList()
    }

    fun getTypeUrl(type: String): String {
        var resultTypes = ""
        val cardTypes = type.split(' ')
        for (cardType in cardTypes) {
            resultTypes += getActiveConcept()?.types[cardType] ?: ""
        }
        return resultTypes
    }

    fun getRawRarity(rarity: String): String {
        return getActiveConcept()?.reverseRarities[rarity] ?: ""
    }

    fun getPrettyRarity(rarity: String): String {
        return getActiveConcept()?.rarities[rarity] ?: ""
    }

    fun getParallelRarity(rarity: String): String {
        return getActiveConcept()?.parallel[rarity] ?: ""
    }

    fun getDiamondRarities(): List<String> {
        val output = mutableListOf<String>()
        getActiveConcept()?.rarities?.forEach{ rarity ->
            if (rarity.key.endsWith("DIAMOND")) {
                output.add(rarity.key)
            }
        }
        return output
    }

    fun getStarCrownRarities(): List<String> {
        val output = mutableListOf<String>()
        getActiveConcept()?.rarities?.forEach{ rarity ->
            if (rarity.key.endsWith("STAR") || rarity.key.endsWith("CROWN")) {
                output.add(rarity.key)
            }
        }
        return output
    }

    fun getShinyAnomalyRarities(): List<String> {
        val output = mutableListOf<String>()
        getActiveConcept()?.rarities?.forEach{ rarity ->
            if (rarity.key.endsWith("SHINY") || rarity.key.endsWith("ANOMALY")) {
                output.add(rarity.key)
            }
        }
        return output
    }
}