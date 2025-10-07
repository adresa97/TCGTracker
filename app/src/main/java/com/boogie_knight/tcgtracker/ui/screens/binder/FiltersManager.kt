package com.boogie_knight.tcgtracker.ui.screens.binder

import com.boogie_knight.tcgtracker.models.SQLFilterConfig
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.services.Concepts
import com.boogie_knight.tcgtracker.services.SetsData

enum class FilterType(val value: String) {
    RARITY("Rarity"),
    SET("Set")
}

object FiltersManager {
    private val rarityFiltersMap = Concepts.getRarities()
        .associateBy({key -> key}, {key -> true})
        .toMutableMap()

    private val setFiltersMap = SetsData.getSetIDs()
        .associateBy({key -> key}, {key -> true})
        .toMutableMap()

    fun initiateFilters() {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        rarityFiltersMap.forEach { rarity ->
            val storedState = UserRepository.getFilterState(
                filter = rarity.key,
                type = FilterType.RARITY.value
            )
            if (storedState == null) {
                toSaveList.add(SQLFilterConfig(
                    filter = rarity.key,
                    type = FilterType.RARITY.value,
                    state = 1
                ))
            }
            else rarityFiltersMap[rarity.key] = storedState
        }

        setFiltersMap.forEach { set ->
            val storedState = UserRepository.getFilterState(
                filter = set.key,
                type = FilterType.SET.value
            )
            if (storedState == null) {
                toSaveList.add(SQLFilterConfig(
                    filter = set.key,
                    type = FilterType.SET.value,
                    state = 1
                ))
            }
            else setFiltersMap[set.key] = storedState
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun addFilter(filter: String) {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        if (rarityFiltersMap.containsKey(filter)) {
            rarityFiltersMap[filter] = true
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                type = FilterType.RARITY.value,
                state = 1
            ))

            val parallel = Concepts.getParallelRarity(filter)
            if (rarityFiltersMap.containsKey(parallel)) {
                rarityFiltersMap[parallel] = true
            }
        }
        else if (setFiltersMap.containsKey(filter)) {
            setFiltersMap[filter] = true
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                type = FilterType.SET.value,
                state = 1
            ))
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun removeFilter(filter: String) {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        if (rarityFiltersMap.containsKey(filter)) {
            rarityFiltersMap[filter] = false
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                type = FilterType.RARITY.value,
                state = 0
            ))

            val parallel = Concepts.getParallelRarity(filter)
            if (rarityFiltersMap.containsKey(parallel)) {
                rarityFiltersMap[parallel] = false
            }
        }
        else if (setFiltersMap.containsKey(filter)) {
            setFiltersMap[filter] = false
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                type = FilterType.SET.value,
                state = 0
            ))
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun getRarityFilters(): Map<String, Boolean> {
        return rarityFiltersMap
    }

    fun getSetFilters(): Map<String, Boolean> {
        return setFiltersMap
    }

    fun getActiveRarityFilters(): List<String> {
        val activeFilters = mutableListOf<String>()
        rarityFiltersMap.forEach{ filter ->
            if (filter.value) activeFilters.add(filter.key)
        }
        return activeFilters
    }

    fun getActiveSetFilters(): List<String> {
        val activeFilters = mutableListOf<String>()
        setFiltersMap.forEach{ filter ->
            if (filter.value) activeFilters.add(filter.key)
        }
        return activeFilters
    }

    fun getInactiveRarityFilters(): List<String> {
        val inactiveFilters = mutableListOf<String>()
        rarityFiltersMap.forEach{ filter ->
            if (!filter.value) inactiveFilters.add(filter.key)
        }
        return inactiveFilters
    }

    fun getInactiveSetFilters(): List<String> {
        val inactiveFilters = mutableListOf<String>()
        setFiltersMap.forEach{ filter ->
            if (!filter.value) inactiveFilters.add(filter.key)
        }
        return inactiveFilters
    }

    fun areAllRarityFiltersActivated(activatedFilters: List<String>): Boolean {
        return activatedFilters.containsAll(rarityFiltersMap.keys)
    }

    fun areAllSetFiltersActivated(activatedFilters: List<String>): Boolean {
        return activatedFilters.containsAll(setFiltersMap.keys)
    }

    fun isFilterActivated(filter: String, isPretty: Boolean): Boolean {
        val id =
            if (isPretty) {
                val rawRarity = Concepts.getRawRarity(filter)
                if (rawRarity != "") rawRarity
                else SetsData.getSetID(filter)
            }
            else filter

        if (rarityFiltersMap.containsKey(id)) return rarityFiltersMap[id] ?: false
        return setFiltersMap[id] ?: false
    }
}