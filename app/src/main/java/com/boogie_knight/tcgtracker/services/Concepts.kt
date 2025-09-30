package com.boogie_knight.tcgtracker.services

import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.JsonConcepts
import com.boogie_knight.tcgtracker.utils.ParseJSON

const val ASSETS_CONCEPTS_DATA_FILE_PATH = "PTCGPocket/concepts.json"

object Concepts {
    private val types = mutableMapOf<String, String>()
    private val rarities = mutableMapOf<String, String>()
    private val reverseRarities = mutableMapOf<String, String>()
    private val parallel = mutableMapOf<String, String>()

    fun loadJSONData() {
        val jsonString = AssetsRepository.getData(ASSETS_CONCEPTS_DATA_FILE_PATH)
        val jsonData = ParseJSON(jsonString, JsonConcepts::class.java)
        if (jsonData != null) {
            types.putAll(jsonData.types)
            rarities.putAll(jsonData.rarities)
            if (jsonData.parallel != null) parallel.putAll(jsonData.parallel)
        }

        rarities.forEach { rarity ->
            reverseRarities.put(key = rarity.value, value = rarity.key)
        }
    }

    fun getTypes(): List<String> {
        return types.keys.toList()
    }
    
    fun getTypeUrls(): List<String> {
        return types.values.toList()
    }

    fun getRarities(): List<String> {
        return rarities.keys.toList()
    }

    fun getPrettyRarities(): List<String> {
        return rarities.values.toList()
    }

    fun getTypeUrl(type: String): String {
        return types[type] ?: ""
    }

    fun getRawRarity(rarity: String): String {
        return reverseRarities[rarity] ?: ""
    }

    fun getPrettyRarity(rarity: String): String {
        return rarities[rarity] ?: ""
    }

    fun getParallelRarity(rarity: String): String {
        return parallel[rarity] ?: ""
    }

    fun getDiamondRarities(): List<String> {
        val output = mutableListOf<String>()
        rarities.forEach{ rarity ->
            if (rarity.key.contains("DIAMOND")) {
                output.add(rarity.key)
            }
        }
        return output
    }

    fun getStarCrownRarities(): List<String> {
        val output = mutableListOf<String>()
        rarities.forEach{ rarity ->
            if (rarity.key.contains("STAR") || rarity.key.contains("CROWN")) {
                output.add(rarity.key)
            }
        }
        return output
    }

    fun getShinyRarities(): List<String> {
        val output = mutableListOf<String>()
        rarities.forEach{ rarity ->
            if (rarity.key.contains("SHINY")) {
                output.add(rarity.key)
            }
        }
        return output
    }
}