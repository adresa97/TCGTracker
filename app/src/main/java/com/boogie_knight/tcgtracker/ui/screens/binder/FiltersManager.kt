package com.boogie_knight.tcgtracker.ui.screens.binder

import com.boogie_knight.tcgtracker.models.SQLFilterConfig
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.services.Concepts
import com.boogie_knight.tcgtracker.services.SetsData
import kotlin.collections.associateBy

enum class FilterType(val value: String) {
    RARITY("Rarity"),
    SET("Set")
}

object FiltersManager {
    private val rarityFiltersMap = Concepts.getGlobalRaritiesMap().mapValues{ rarities->
        rarities.value.associateBy({key -> key}, {key -> true})
            .toMutableMap()
    }.toMutableMap()

    private val setFiltersMap = SetsData.getGlobalBoosterIDsMap().mapValues{ rarities->
        rarities.value.associateBy({key -> key}, {key -> true})
            .toMutableMap()
    }.toMutableMap()

    fun initiateFilters() {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        rarityFiltersMap.forEach { game ->
            val storedStates = mutableMapOf<String, Boolean>()
            game.value.forEach{ rarity->
                val storedState = UserRepository.getFilterState(
                    filter = rarity.key,
                    game = game.key,
                    type = FilterType.RARITY.value
                )
                if (storedState == null) {
                    toSaveList.add(SQLFilterConfig(
                        filter = rarity.key,
                        game = game.key,
                        type = FilterType.RARITY.value,
                        state = 1
                    ))
                }
                else storedStates.put(rarity.key, storedState)
            }
            if (!storedStates.isEmpty()) rarityFiltersMap[game.key] = storedStates
        }

        setFiltersMap.forEach { game ->
            val storedStates = mutableMapOf<String, Boolean>()
            game.value.forEach { set ->
                val storedState = UserRepository.getFilterState(
                    filter = set.key,
                    game = game.key,
                    type = FilterType.SET.value
                )
                if (storedState == null) {
                    toSaveList.add(
                        SQLFilterConfig(
                            filter = set.key,
                            game = game.key,
                            type = FilterType.SET.value,
                            state = 1
                        )
                    )
                } else storedStates.put(set.key, storedState)
            }
            if (!storedStates.isEmpty()) setFiltersMap[game.key] = storedStates
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun addFilter(game: String, filter: String) {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        var filtersMap = rarityFiltersMap[game]
        var filterType = FilterType.RARITY.value
        if (filtersMap.isNullOrEmpty() || !filtersMap.containsKey(filter)) {
            filtersMap = setFiltersMap[game]
            filterType = FilterType.SET.value
        }
        if (!filtersMap.isNullOrEmpty() && filtersMap.contains(filter)) {
            filtersMap[filter] = true
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                game = game,
                type = filterType,
                state = 1
            ))

            val parallel = Concepts.getParallelRarity(game, filter)
            if (parallel != "" && filtersMap.containsKey(filter)) {
                filtersMap[filter] = true
                toSaveList.add(SQLFilterConfig(
                    filter = filter,
                    game = game,
                    type = filterType,
                    state = 1
                ))
            }
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun removeFilter(game: String, filter: String) {
        val toSaveList = mutableListOf<SQLFilterConfig>()

        var filtersMap = rarityFiltersMap[game]
        var filterType = FilterType.RARITY.value
        if (filtersMap.isNullOrEmpty() || !filtersMap.containsKey(filter)) {
            filtersMap = setFiltersMap[game]
            filterType = FilterType.SET.value
        }
        if (!filtersMap.isNullOrEmpty() && filtersMap.contains(filter)) {
            filtersMap[filter] = false
            toSaveList.add(SQLFilterConfig(
                filter = filter,
                game = game,
                type = filterType,
                state = 0
            ))

            val parallel = Concepts.getParallelRarity(game, filter)
            if (parallel != "" && filtersMap.containsKey(filter)) {
                filtersMap[filter] = false
                toSaveList.add(SQLFilterConfig(
                    filter = filter,
                    game = game,
                    type = filterType,
                    state = 0
                ))
            }
        }

        UserRepository.saveFilters(toSaveList)
    }

    fun getRarityFilters(game: String): Map<String, Boolean> {
        val rarities = rarityFiltersMap[game]
        return rarities ?: mapOf()
    }

    fun getSetFilters(game: String): Map<String, Boolean> {
        val sets = setFiltersMap[game]
        return sets ?: mapOf()
    }

    fun getActiveRarityFilters(game: String): List<String> {
        val rarities = rarityFiltersMap[game]
        if (rarities.isNullOrEmpty()) return listOf()

        val activeFilters = mutableListOf<String>()
        rarities.forEach{ filter ->
            if (filter.value) activeFilters.add(filter.key)
        }
        return activeFilters
    }

    fun getActiveSetFilters(game: String): List<String> {
        val sets = setFiltersMap[game]
        if (sets.isNullOrEmpty()) return listOf()

        val activeFilters = mutableListOf<String>()
        sets.forEach{ filter ->
            if (filter.value) activeFilters.add(filter.key)
        }
        return activeFilters
    }

    fun getInactiveRarityFilters(game: String): List<String> {
        val rarities = rarityFiltersMap[game]
        if (rarities.isNullOrEmpty()) return listOf()

        val inactiveFilters = mutableListOf<String>()
        rarities.forEach{ filter ->
            if (!filter.value) inactiveFilters.add(filter.key)
        }
        return inactiveFilters
    }

    fun getInactiveSetFilters(game: String): List<String> {
        val sets = setFiltersMap[game]
        if (sets.isNullOrEmpty()) return listOf()

        val inactiveFilters = mutableListOf<String>()
        sets.forEach{ filter ->
            if (!filter.value) inactiveFilters.add(filter.key)
        }
        return inactiveFilters
    }

    fun areAllRarityFiltersActivated(game: String, activatedFilters: List<String>): Boolean {
        val rarities = rarityFiltersMap[game]
        if (rarities.isNullOrEmpty()) return false
        return activatedFilters.containsAll(rarityFiltersMap[game]!!.keys)
    }

    fun areAllSetFiltersActivated(activatedFilters: List<String>): Boolean {
        return activatedFilters.containsAll(setFiltersMap.keys)
    }

    fun isFilterActivated(game: String, filter: String, isPretty: Boolean): Boolean {
        val id =
            if (isPretty) {
                val rawRarity = Concepts.getRawRarity(game, filter)
                if (rawRarity != "") rawRarity
                else SetsData.getSetID(game, filter)
            }
            else filter

        var filterMap = rarityFiltersMap[game]
        if (filterMap.isNullOrEmpty() || !filterMap.containsKey(id)) filterMap = setFiltersMap[game]
        if (!filterMap.isNullOrEmpty() && filterMap.containsKey(id)) return true
        return false
    }
}