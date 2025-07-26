package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson

val coverMap = mapOf<String, Int>(
    Pair("A-PROMO", R.drawable.a_promo_en),
    Pair("A1", R.drawable.a1_genetic_apex_en),
    Pair("A1a", R.drawable.a1a_mythical_island_en),
    Pair("A2", R.drawable.a2_spacetime_smackdown_en),
    Pair("A2a", R.drawable.a2a_triumphant_light_en),
    Pair("A2b", R.drawable.a2b_shining_revelry_en),
    Pair("A3", R.drawable.a3_celestial_guardians_en),
    Pair("A3a", R.drawable.a3a_extradimensional_crisis_en),
    Pair("A3b", R.drawable.a3b_eevee_grove_en)
)

val emptyCover = R.drawable.a_promo_en

data class Set(
    val series: String,
    val expansion: String,
    val set: String,
    val name: String,
    var cover: Int
)

class CollectionData(applicationContext: Context, jsonPath: String) {
    val collections: Array<Set>
    init {
        val jsonString = ReadJSONFromAssets(applicationContext, "collections.json")
        collections = Gson().fromJson(jsonString, Array<Set>::class.java)
        collections.forEach { set ->
            set.cover = coverMap[set.set] ?: emptyCover
        }
    }

    fun getSeriesMap(): Map<String, List<Set>> {
        return collections.groupBy({set -> set.series})
    }

    fun getExpansionsMap(series: String): Map<String, List<Set>> {
        return getSeriesMap().getOrDefault(series, listOf()).groupBy({set -> set.expansion})
    }
}