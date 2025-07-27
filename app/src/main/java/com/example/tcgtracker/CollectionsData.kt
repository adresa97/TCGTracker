package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.models.coverMap
import com.example.tcgtracker.models.emptyCover
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson

class CollectionsData(applicationContext: Context, jsonPath: String) {
    val collections: Array<Set>
    init {
        val jsonString = ReadJSONFromAssets(applicationContext, "collections.json")
        collections = Gson().fromJson(jsonString, Array<Set>::class.java)
        collections.forEach { set ->
            set.cover = coverMap[set.set] ?: emptyCover
        }
    }

    fun getSeriesMap(): Map<String, List<Set>> {
        return collections.groupBy({ set -> set.series })
    }
}