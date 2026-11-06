package com.boogie_knight.tcgtracker.services

import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonConcepts
import com.boogie_knight.tcgtracker.utils.ParseJSON

object Concepts {
    data class ConceptData (
        val types: Map<String, String>,
        val rarities: Map<String, String>,
        val common: List<String>,
        val uncommon: List<String>,
        val rare: List<String>,
        val reverseRarities: Map<String, String>,
        val parallel: Map<String, String>
    )

    private val conceptDataMap = mutableMapOf<String, ConceptData>()

    fun loadJSONData() {
        val games = GamesData.getGamesList()
        for (game in games) {
            val jsonString = AssetsRepository.getData(GamesData.getGameConceptsRoute(game))
            val jsonData = ParseJSON(jsonString, JsonConcepts::class.java)
            if (jsonData != null) {
                val reverseRarities = mutableMapOf<String, String>()
                jsonData.rarities.forEach { rarity ->
                    reverseRarities.put(key = rarity.value, value = rarity.key)
                }
                conceptDataMap.put(game, ConceptData(
                    types = jsonData.types,
                    rarities = jsonData.rarities,
                    common = jsonData.common,
                    uncommon = jsonData.uncommon,
                    rare = jsonData.rare,
                    reverseRarities = reverseRarities,
                    parallel = jsonData.parallel ?: mapOf()
                ))
            }
        }
    }

    private fun getGameConcepts(game: String): ConceptData? {
        return conceptDataMap[game]
    }

    fun getTypes(game: String): List<String> {
        return getGameConcepts(game)?.types?.keys?.toList() ?: emptyList()
    }
    
    fun getTypeUrls(game: String): List<String> {
        return getGameConcepts(game)?.types?.values?.toList() ?: emptyList()
    }

    fun getGlobalRaritiesMap(): Map<String, List<String>> {
        val raritiesMap = mutableMapOf<String, List<String>>()
        conceptDataMap.forEach{ game->
            raritiesMap.put(game.key, game.value.rarities.keys.toList())
        }
        return raritiesMap
    }

    fun getRarities(game: String): List<String> {
        return getGameConcepts(game)?.rarities?.keys?.toList() ?: emptyList()
    }

    fun getPrettyRarities(game: String): List<String> {
        return getGameConcepts(game)?.rarities?.values?.toList() ?: emptyList()
    }

    fun getTypeUrl(game: String, type: String): String {
        var resultTypes = ""
        val cardTypes = type.split(' ')
        for (cardType in cardTypes) {
            resultTypes += getGameConcepts(game)?.types[cardType] ?: ""
        }
        return resultTypes
    }

    fun getRawRarity(game: String, rarity: String): String {
        return getGameConcepts(game)?.reverseRarities[rarity] ?: ""
    }

    fun getPrettyRarity(game:String, rarity: String): String {
        return getGameConcepts(game)?.rarities[rarity] ?: ""
    }

    fun getParallelRarity(game: String, rarity: String): String {
        return getGameConcepts(game)?.parallel[rarity] ?: ""
    }

    fun getCommonRarities(game: String): List<String> {
        val concepts = getGameConcepts(game)
        if (concepts == null) listOf<String>()

        val output = mutableListOf<String>()
        concepts!!.common.forEach{ rarity ->
            if (concepts.rarities.containsKey(rarity)) {
                output.add(rarity)
            }
        }
        return output
    }

    fun getUncommonRarities(game: String): List<String> {
        val concepts = getGameConcepts(game)
        if (concepts == null) listOf<String>()

        val output = mutableListOf<String>()
        concepts!!.uncommon.forEach{ rarity ->
            if (concepts.rarities.containsKey(rarity)) {
                output.add(rarity)
            }
        }
        return output
    }

    fun getRareRarities(game: String): List<String> {
        val concepts = getGameConcepts(game)
        if (concepts == null) listOf<String>()

        val output = mutableListOf<String>()
        concepts!!.rare.forEach{ rarity ->
            if (concepts.rarities.containsKey(rarity)) {
                output.add(rarity)
            }
        }
        return output
    }

    fun isRarityInGame(game: String, rarity: String): Boolean {
        return getGameConcepts(game)?.rarities?.containsKey(rarity) ?: false
    }
}