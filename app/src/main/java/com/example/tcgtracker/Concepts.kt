package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.JsonConcepts
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson

const val ASSETS_CONCEPTS_DATA_FILE_PATH = "PTCGPocket/concepts.json"

object Concepts {
    private val types = mutableMapOf<String, String>()
    private val rarities = mutableMapOf<String, String>()

    fun loadJSONData(context: Context) {
        val jsonString = ReadJSONFromAssets(context, ASSETS_CONCEPTS_DATA_FILE_PATH)
        val jsonData = Gson().fromJson(jsonString, JsonConcepts::class.java)
        types.putAll(jsonData.types)
        rarities.putAll(jsonData.rarities)
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

    fun getPrettyRarity(rarity: String): String {
        return rarities[rarity] ?: ""
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