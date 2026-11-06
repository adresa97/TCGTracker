package com.boogie_knight.tcgtracker.services

import androidx.compose.ui.graphics.Color
import com.boogie_knight.tcgtracker.models.Card
import com.boogie_knight.tcgtracker.models.InnerJsonSet
import com.boogie_knight.tcgtracker.models.Origin
import com.boogie_knight.tcgtracker.models.OwnedBoosterData
import com.boogie_knight.tcgtracker.models.OwnedData
import com.boogie_knight.tcgtracker.models.OwnedSetData
import com.boogie_knight.tcgtracker.models.SQLOwnedSet
import com.boogie_knight.tcgtracker.models.Set
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.utils.ParseJSON
import kotlin.math.pow

object SetsData {
    private val setListMap = mutableMapOf<String, MutableList<Set>>()

    // Populate collections list
    fun loadJSONData() {
        val games = GamesData.getGamesList()
        for (game in games)
        {
            // If data already stored end function
            if (!setListMap.containsKey(game)) setListMap.put(game, mutableListOf<Set>())
            if (!(setListMap[game]?.isEmpty() ?: true)) return

            // Get inner data (objective data of every set)
            val innerJsonString = AssetsRepository.getData(GamesData.getGameSetsRoute(game))
            val innerJsonData = ParseJSON(innerJsonString, Array<InnerJsonSet>::class.java)?.asList() ?: listOf()

            // Map data into collection
            innerJsonData.forEach { set ->
                // Turn set color string to Color object
                val color = Color(set.color[0] / 255, set.color[1] / 255, set.color[2] / 255)

                // Get precalculations
                val precalculations = getPrecalculations(game, set.set, set.origins)

                // Add this set entry to collections.
                // If calculated numbers aren't stored in user data, calculate them from cards data
                setListMap[game]?.add(
                    Set(
                        series = set.series,
                        expansion = set.expansion,
                        set = set.set,
                        name = set.name,
                        cover = set.cover,
                        color = color,
                        cardCount = set.cardCount,
                        origins = set.origins,
                        numbers = precalculations ?: calculateNumbers(
                            game = game,
                            set = set.set,
                            cardList = CardsData.getCardList(game,set.set)
                        )
                    )
                )
            }
        }
    }

    fun getGlobalList(): List<Set> {
        val setList = mutableListOf<Set>()
        setListMap.forEach{ list ->
            setList.addAll(list.value)
        }
        return setList
    }

    // Get user precalculations
    private fun getPrecalculations(game: String, set: String, origins: List<String>): OwnedSetData? {
        // Full set data
        val setData = UserRepository.getSetPrecalculations(game = game, set = set)
        if (setData.second <= 0) return null
        val all = OwnedData(
            game = game,
            ownedCards = setData.first,
            totalCards = setData.second
        )

        // From each rarity data
        val byRarity = mutableMapOf<String, OwnedData>()
        Concepts.getRarities(game).forEach{ rarity ->
            // Get this rarity data
            val rarityData = UserRepository.getSetPrecalculations(game = game, set = set, rarity = rarity)
            if (rarityData.second > 0) {
                val ownedRarity = OwnedData(
                    game = game,
                    ownedCards = rarityData.first,
                    totalCards = rarityData.second
                )
                byRarity.put(rarity, ownedRarity)
            }
        }

        // From each origin data
        val byBooster = mutableMapOf<String, OwnedBoosterData>()
        origins.forEach{ origin ->
            // Get this origin general data
            val originData = UserRepository.getSetPrecalculations(game = game, set = set, booster = origin)
            val ownedOrigin = OwnedData(
                game = game,
                ownedCards = originData.first,
                totalCards = originData.second
            )

            if (OriginsData.isOriginPromo(origin)) {
                byBooster.put(origin, OwnedBoosterData(
                    game = game,
                    all = ownedOrigin,
                    byRarity = null
                )
                )
            } else {
                val byBoosterRarity = mutableMapOf<String, OwnedData>()
                Concepts.getRarities(game).forEach{ rarity ->
                    val originRarityData = UserRepository.getSetPrecalculations(set, origin, rarity)
                    if (originRarityData.second > 0) {
                        val ownedBoosterRarity = OwnedData(
                            game = game,
                            ownedCards = originRarityData.first,
                            totalCards = originRarityData.second
                        )
                        byBoosterRarity.put(rarity, ownedBoosterRarity)
                    }
                }
                byBooster.put(origin, OwnedBoosterData(
                    game = game,
                    all = ownedOrigin,
                    byRarity = byBoosterRarity
                )
                )
            }
        }

        return OwnedSetData(
            game = game,
            all = all,
            byBooster = byBooster,
            byRarity = byRarity.ifEmpty { null }
        )
    }

    // Calculate all needed numbers from a list of cards and boosters
    private fun calculateNumbers(game: String, set: String, cardList: List<Card>): OwnedSetData {
        // List of data to insert or replace
        val dataList = mutableListOf<SQLOwnedSet>()

        // Full set card data
        val allOwned = cardList.stream().filter{ card -> card.owned }.count().toInt()
        val allTotal = cardList.count()
        val all = OwnedData(
            game = game,
            ownedCards = allOwned,
            totalCards = allTotal
        )
        dataList.add(
            SQLOwnedSet(
                set = set,
                game = game,
                owned = allOwned,
                total = allTotal
            )
        )

        // Calculate total and owned cards from each rarity
        val byRarity = mutableMapOf<String, OwnedData>()
        Concepts.getRarities(game).forEach{ rarity ->
            // Get this rarity list of cards (including extra ones)
            val rarityCards = cardList.stream().filter{ card -> card.rarity == rarity }
                .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

            // If there is any card of this rarity
            if (rarityCards.isNotEmpty()) {
                // Calculate total and owned cards count
                val rarityOwned = rarityCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
                val rarityTotal = rarityCards.count()
                val ownedRarityData = OwnedData(
                    game = game,
                    ownedCards = rarityOwned,
                    totalCards = rarityTotal
                )
                dataList.add(
                    SQLOwnedSet(
                        set = set,
                        game = game,
                        rarity = rarity,
                        owned = rarityOwned,
                        total = rarityTotal
                    )
                )

                // Add this data to rarity map
                byRarity.put(rarity, ownedRarityData)
            }
        }

        // Get list of boosters of this list of cards
        val boosters = mutableListOf<String>()
        cardList.forEach { card ->
            val cardOrigins = card.origins
            if (!cardOrigins.isEmpty()) {
                val mainOrigin = cardOrigins[0]
                if (!boosters.contains(mainOrigin)) boosters.add(mainOrigin)
            }
        }

        // Calculate total and owned cards from each booster and rarity
        val byBooster = mutableMapOf<String, OwnedBoosterData>()
        boosters.forEach{ booster ->
            // Get this booster list of cards (excluding extra ones, as those doesn't count towards pull probability)
            val boosterCards = cardList.stream().filter{ card -> card.origins.contains(booster)}
                .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

            // Calculate general total and owned cards count
            val boosterOwned = boosterCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
            val boosterTotal = boosterCards.count()
            val ownedBoosterData = OwnedData(
                game = game,
                ownedCards = boosterOwned,
                totalCards = boosterTotal
            )
            dataList.add(
                SQLOwnedSet(
                    game = game,
                    set = set,
                    booster = booster,
                    owned = boosterOwned,
                    total = boosterTotal
                )
            )

            // If promo booster put data in map, otherwise calculate for each rarity
            if (OriginsData.isOriginPromo(booster)) {
                byBooster.put(booster, OwnedBoosterData(
                    game = game,
                    all = ownedBoosterData,
                    byRarity = null
                )
                )
            } else {
                val rarityBoosterData = mutableMapOf<String, OwnedData>()
                Concepts.getRarities(game).forEach{ rarity ->
                    // Get list of cards from this booster and rarity
                    val rarityCards = boosterCards.stream().filter{ card -> card?.rarity == rarity && !card.extra }
                        .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

                    // Calculate total and owned cards count
                    val rarityOwned = rarityCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
                    val rarityTotal = rarityCards.count()
                    val ownedRarityData = OwnedData(
                        game = game,
                        ownedCards = rarityOwned,
                        totalCards = rarityTotal
                    )
                    dataList.add(
                        SQLOwnedSet(
                            game = game,
                            set = set,
                            booster = booster,
                            rarity = rarity,
                            owned = rarityOwned,
                            total = rarityTotal
                        )
                    )

                    // Add this data to rarity map
                    rarityBoosterData.put(rarity, ownedRarityData)
                }
                byBooster.put(booster, OwnedBoosterData(
                    game = game,
                    all = ownedBoosterData,
                    byRarity = rarityBoosterData
                )
                )
            }
        }

        UserRepository.saveSets(dataList)

        return OwnedSetData(
            game = game,
            all = all,
            byBooster = byBooster,
            byRarity = byRarity.ifEmpty { null }
        )
    }

    fun recalculateSetData(game: String, cardList: List<Card>, setCode: String) {
        val newCalculations = calculateNumbers(game, setCode, cardList)
        getGlobalList().firstOrNull{ set -> set.set == setCode }?.numbers = newCalculations
    }

    // Get all sets of a game, mapped by series
    fun getSeriesMap(game: String): Map<String, List<Set>> {
        val setList = setListMap[game]
        if (setList == null) return mapOf<String, List<Set>>()
        return setList.groupBy({ set -> set.series })
    }

    // Get a set from its ID
    fun getSetFromID(id: String): Set? {
        return getGlobalList().firstOrNull{ set -> set.set == id }
    }

    fun getGlobalSetIDs(): List<String> {
        val ids = mutableListOf<String>()
        getGlobalList().forEach{ set ->
            ids.add(set.set)
        }
        return ids
    }

    fun getGlobalBoosterIDsMap(): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        setListMap.forEach{ game->
            val ids = mutableListOf<String>()
            game.value.forEach{ set ->
                val originsCount = set.origins.size
                var i = 0
                var isSpecial = false
                while (i < originsCount && !isSpecial) {
                    if (OriginsData.isOriginSpecial(set.origins[i])) isSpecial = true
                    i++
                }
                if (!isSpecial) ids.add(set.set)
            }
            map.put(game.key, ids)
        }
        return map
    }

    fun getGlobalBoosterSetIDs(): List<String> {
        val ids = mutableListOf<String>()
        getGlobalList().forEach{ set ->
            val originsCount = set.origins.size
            var index = 0
            var isSpecial = false
            while (index < originsCount && !isSpecial) {
                if (OriginsData.isOriginSpecial(set.origins[index])) isSpecial = true
                index++
            }
            if (!isSpecial) ids.add(set.set)
        }
        return ids
    }

    fun getGlobalSpecialSetIDs(): List<String> {
        val ids = mutableListOf<String>()
        getGlobalList().forEach{ set ->
            val originsCount = set.origins.size
            var index = 0
            var isSpecial = false
            while (index < originsCount && !isSpecial) {
                if (OriginsData.isOriginSpecial(set.origins[index]))
                {
                    isSpecial = true
                    ids.add(set.set)
                }
                index++
            }
        }
        return ids
    }

    // Get all sets IDs
    fun getSetIDs(game: String): List<String> {
        val setList = setListMap[game]
        if (setList == null) return listOf<String>()
        val ids = mutableListOf<String>()
        setList.forEach{ set ->
            ids.add(set.set)
        }
        return ids
    }

    // Get set id from its name
    fun getSetID(name: String): String {
        return getGlobalList().firstOrNull{ set -> (set.name.es == name || set.name.en == name) }?.set ?: name
    }

    // Get set name from its code
    fun getSetName(code: String): String {
        return getGlobalList().firstOrNull{ set -> set.set == code }?.name?.es ?: code
    }

    // Get map of set color assign to set ids
    fun getSetColors(game: String): Map<String, Color> {
        val setList = setListMap[game]
        if (setList == null) return mapOf<String, Color>()
        val outputMap = mutableMapOf<String, Color>()
        setList.forEach { set ->
            outputMap.put(set.set, set.color)
        }
        return outputMap
    }

    // Get set color form its code
    fun getSetColor(value: String): Color? {
        val set = getGlobalList().firstOrNull{ set -> (set.name.es == value || set.set == value) }
        return set?.color
    }

    // Get most probable set
    fun getMostProbableSet(
        game: String,
        rarities: List<String> = listOf()
    ): Pair<Set, Origin>? {
        val setList = setListMap[game]
        if (setList == null || setList.isEmpty()) return null

        var probableSet: Set? = null
        var probableBooster: Origin? = null
        var probableOdd = 0.0f
        setList.forEach{ set ->
            val setOrigin = getMostProbableBooster(game, set.set, rarities)
            if (setOrigin != null) {
                if (setOrigin.second >= probableOdd) {
                    probableOdd = setOrigin.second
                    probableBooster = setOrigin.first
                    probableSet = set
                }
            }
        }

        if (probableSet == null || probableBooster == null) return null
        return Pair(probableSet, probableBooster)
    }

    // Get most probable out of a list of sets
    fun getMostProbableSetFromList(
        game: String,
        sets: List<Set>,
        rarities: List<String> = listOf()
    ): Pair<Set, Origin>? {
        if (sets.isEmpty()) return null

        var probableSet: Set? = null
        var probableBooster: Origin? = null
        var probableOdd = 0.0f
        sets.forEach{ set ->
            val setOrigin = getMostProbableBooster(game, set.set, rarities)
            if (setOrigin != null) {
                if (setOrigin.second >= probableOdd) {
                    probableOdd = setOrigin.second
                    probableBooster = setOrigin.first
                    probableSet = set
                }
            }
        }

        if (probableSet == null || probableBooster == null) return null
        return Pair(probableSet, probableBooster)
    }

    // Get most probable booster from one set
    fun getMostProbableBooster(
        game: String,
        setID: String,
        rarities: List<String> = listOf()
    ): Pair<Origin, Float>? {
        val set = getGlobalList().find{ set -> set.set == setID }
        if (set == null) return null

        val setOrigins = set.origins.filter{ origin -> OriginsData.getOriginType(game, origin) != "PROMO" }
        if (setOrigins.isEmpty())  return null

        var probableOrigin: Origin? = null
        var probableOdd = 0.0f
        setOrigins.forEach{ origin ->
            val originObject = OriginsData.getOriginByID(game, origin)
            if (originObject != null) {
                val probabilities = getBoosterRemainingOdds(setID, originObject, rarities)
                var totalProbability = 1.0f
                for (i in 0 until probabilities.size) {
                    totalProbability *= (1.0f - probabilities[i])
                }
                totalProbability = (1.0f - totalProbability) * 100.0f
                if (totalProbability > 99.9f) totalProbability = 100.0f
                else if (totalProbability < 0.0f) totalProbability = 0.0f
                if (totalProbability >= probableOdd) {
                    probableOdd = totalProbability
                    probableOrigin = originObject
                }
            }
        }

        if (probableOrigin == null) return null
        return Pair(probableOrigin, probableOdd)
    }

    // Get a booster remaining cards odds
    fun getBoosterRemainingOdds(
        setID: String,
        origin: Origin,
        rarities: List<String> = listOf()
    ): List<Float> {
        val packSize = origin.packSize ?: 0
        val probabilities = mutableListOf<Float>()
        for (i in 0 until packSize) {
            probabilities.add(0.0f)
        }

        if (origin.type == "PROMO" || origin.odds == null) return probabilities

        val set = getGlobalList().find{ set -> set.set == setID }
        if (set == null) return probabilities
        if (!set.origins.contains(origin.id)) return probabilities

        val numbers = set.numbers.byBooster[origin.id]?.byRarity
        if (numbers == null) return probabilities

        val fixedRarities = rarities.ifEmpty { origin.odds.keys.toList() }

        fixedRarities.forEach{ rarity ->
            if (numbers.containsKey(rarity)) {
                val totalCards = numbers[rarity]!!.totalCards
                if (totalCards != 0) {
                    val remainingCards = totalCards - numbers[rarity]!!.ownedCards
                    for (i in 0 until packSize) {
                        val cardOdd = origin.odds[rarity]?.get(i) ?: 0.0f
                        val totalOdd = (remainingCards * cardOdd) / 100.0f
                        probabilities[i] += totalOdd
                        if (probabilities[i] > 1.0f) probabilities[i] = 1.0f
                        else if (probabilities[i] < 0.0f) probabilities[i] = 0.0f
                    }
                }
            }
        }

        return probabilities
    }
}