package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.models.coverMap
import com.example.tcgtracker.models.emptyCover
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson

class CollectionsData() {
    private var collections = listOf<Set>()

    fun loadJSONData(applicationContext: Context, jsonPath: String) {
        if (!collections.isEmpty()) return

        val jsonString = ReadJSONFromAssets(applicationContext, jsonPath)
        collections = Gson().fromJson(jsonString, Array<Set>::class.java).asList()
        collections.forEach { set ->
            set.cover = coverMap[set.set] ?: emptyCover
        }
    }

    fun getSeriesMap(): Map<String, List<Set>> {
        return collections.groupBy({ set -> set.series })
    }

    fun getSetName(code: String): String {
        return collections.firstOrNull({ set -> set.set == code })?.name ?: code
    }
}