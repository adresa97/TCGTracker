package com.example.tcgtracker

object FiltersManager {
    private val filtersMap = Concepts.getRarities()
        .associateBy({key -> key}, {key -> true})
        .toMutableMap()

    fun addFilter(rarity: String) {
        if (filtersMap.containsKey(rarity)) {
            filtersMap[rarity] = true
        }
    }

    fun removeFilter(rarity: String) {
        if (filtersMap.containsKey(rarity)) {
            filtersMap[rarity] = false
        }
    }

    fun getFilters(): Map<String, Boolean> {
        return filtersMap
    }

    fun getActiveFilters(): List<String> {
        val activeFilters = mutableListOf<String>()
        filtersMap.forEach{ filter ->
            if (filter.value) activeFilters.add(filter.key)
        }
        return activeFilters
    }

    fun getInactiveFilters(): List<String> {
        val inactiveFilters = mutableListOf<String>()
        filtersMap.forEach{ filter ->
            if (filter.value) inactiveFilters.add(filter.key)
        }
        return inactiveFilters
    }
}