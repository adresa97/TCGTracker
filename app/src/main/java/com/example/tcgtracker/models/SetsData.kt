package com.example.tcgtracker.models

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson
import kotlin.math.pow

const val ASSETS_SETS_DATA_FILE_PATH = "PTCGPocket/sets.json"

object SetsData {
    private val setList = mutableListOf<Set>()

    // Populate collections list
    fun loadJSONData(context: Context, handler: SQLiteHandler) {
        // If data already stored end function
        if (!setList.isEmpty()) return

        // Get inner data (objective data of every set)
        val innerJsonString = ReadJSONFromAssets(context, ASSETS_SETS_DATA_FILE_PATH)
        val innerJsonData = Gson().fromJson(innerJsonString, Array<InnerJsonSet>::class.java).asList()

        // Map data into collection
        innerJsonData.forEach { set ->
            // Get cover enum from set code
            val cover = CoverImage.from(set.set)

            // Turn set color string to Color object
            val color = Color(set.color[0]/255, set.color[1]/255, set.color[2]/255)

            // Get precalculations
            val precalculations = getPrecalculations(handler, set.set, set.origins)

            // Add this set entry to collections.
            // If calculated numbers aren't stored in user data, calculate them from cards data
            setList.add(Set(
                series = set.series,
                expansion = set.expansion,
                set = set.set,
                name = set.name,
                cover = cover,
                color = color,
                cardCount = set.cardCount,
                origins = set.origins,
                numbers = precalculations ?: calculateNumbers(
                    handler = handler,
                    set = set.set,
                    cardList = CardsData.getCardList(context, handler, set.set)
                )
            ))
        }
    }

    // Get user precalculations
    private fun getPrecalculations(handler: SQLiteHandler, set: String, origins: List<String>): OwnedSetData? {
        // Full set data
        val setData = handler.getSetPrecalculations(set = set)
        if (setData.second <= 0) return null
        val all = OwnedData(
            ownedCards = setData.first,
            totalCards = setData.second
        )

        // From each rarity data
        val byRarity = mutableMapOf<String, OwnedData>()
        Concepts.getRarities().forEach{ rarity ->
            // Get this rarity data
            val rarityData = handler.getSetPrecalculations(set = set, rarity = rarity)
            if (rarityData.second > 0) {
                val ownedRarity = OwnedData(
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
            val originData = handler.getSetPrecalculations(set = set, booster = origin)
            val ownedOrigin = OwnedData(
                ownedCards = originData.first,
                totalCards = originData.second
            )

            if (OriginsData.isOriginPromo(origin)) {
                byBooster.put(origin, OwnedBoosterData(
                    all = ownedOrigin,
                    byRarity = null
                ))
            } else {
                val byBoosterRarity = mutableMapOf<String, OwnedData>()
                Concepts.getRarities().forEach{ rarity ->
                    val originRarityData = handler.getSetPrecalculations(set, origin, rarity)
                    if (originRarityData.second > 0) {
                        val ownedBoosterRarity = OwnedData(
                            ownedCards = originRarityData.first,
                            totalCards = originRarityData.second
                        )
                        byBoosterRarity.put(rarity, ownedBoosterRarity)
                    }
                }
                byBooster.put(origin, OwnedBoosterData(
                    all = ownedOrigin,
                    byRarity = byBoosterRarity
                ))
            }
        }

        return OwnedSetData(
            all = all,
            byBooster = byBooster,
            byRarity = byRarity.ifEmpty { null }
        )
    }

    // Calculate all needed numbers from a list of cards and boosters
    private fun calculateNumbers(handler: SQLiteHandler, set: String, cardList: List<Card>): OwnedSetData {
        // List of data to insert or replace
        val dataList = mutableListOf<SQLOwnedSet>()

        // Full set card data
        val allOwned = cardList.stream().filter{ card -> card.owned }.count().toInt()
        val allTotal = cardList.count()
        val all = OwnedData(
            ownedCards = allOwned,
            totalCards = allTotal
        )
        dataList.add(
            SQLOwnedSet(
                set = set,
                owned = allOwned,
                total = allTotal
            )
        )

        // Calculate total and owned cards from each rarity
        val byRarity = mutableMapOf<String, OwnedData>()
        Concepts.getRarities().forEach{ rarity ->
            // Get this rarity list of cards
            val rarityCards = cardList.stream().filter{ card -> card.rarity == rarity && !card.baby }
                .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

            // If there is any card of this rarity
            if (rarityCards.isNotEmpty()) {
                // Calculate total and owned cards count
                val rarityOwned = rarityCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
                val rarityTotal = rarityCards.count()
                val ownedRarityData = OwnedData(
                    ownedCards = rarityOwned,
                    totalCards = rarityTotal
                )
                dataList.add(
                    SQLOwnedSet(
                        set = set,
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
            // Get this booster list of cards
            val boosterCards = cardList.stream().filter{ card -> card.origins.contains(booster)}
                .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

            // Calculate general total and owned cards count
            val boosterOwned = boosterCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
            val boosterTotal = boosterCards.count()
            val ownedBoosterData = OwnedData(
                ownedCards = boosterOwned,
                totalCards = boosterTotal
            )
            dataList.add(
                SQLOwnedSet(
                    set = set,
                    booster = booster,
                    owned = boosterOwned,
                    total = boosterTotal
                )
            )

            // If promo booster put data in map, otherwise calculate for each rarity
            if (OriginsData.isOriginPromo(booster)) {
                byBooster.put(booster, OwnedBoosterData(
                    all = ownedBoosterData,
                    byRarity = null
                ))
            } else {
                val rarityBoosterData = mutableMapOf<String, OwnedData>()
                Concepts.getRarities().forEach{ rarity ->
                    // Get list of cards from this booster and rarity
                    val rarityCards = boosterCards.stream().filter{ card -> card?.rarity == rarity && !card.baby }
                        .toArray({ size -> arrayOfNulls<Card>(size) }).asList()

                    // Calculate total and owned cards count
                    val rarityOwned = rarityCards.stream().filter{ card -> card?.owned ?: false }.count().toInt()
                    val rarityTotal = rarityCards.count()
                    val ownedRarityData = OwnedData(
                        ownedCards = rarityOwned,
                        totalCards = rarityTotal
                    )
                    dataList.add(
                        SQLOwnedSet(
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
                    all = ownedBoosterData,
                    byRarity = rarityBoosterData
                ))
            }
        }

        handler.saveSetsInBatch(dataList)

        return OwnedSetData(
            all = all,
            byBooster = byBooster,
            byRarity = byRarity.ifEmpty { null }
        )
    }

    fun recalculateSetData(handler: SQLiteHandler, cardList: List<Card>, setCode: String) {
        val newCalculations = calculateNumbers(handler, setCode, cardList)
        setList.firstOrNull{ set -> set.set == setCode }?.numbers = newCalculations
    }

    // Get all sets of a series
    fun getSeriesMap(): Map<String, List<Set>> {
        return setList.groupBy({ set -> set.series })
    }

    // Get a set from its ID
    fun getSetFromID(id: String): Set? {
        return setList.firstOrNull{ set -> set.set == id }
    }

    // Get all sets IDs
    fun getSetIDs(): List<String> {
        val ids = mutableListOf<String>()
        setList.forEach{ set ->
            ids.add(set.set)
        }
        return ids
    }

    // Get set name from its code
    fun getSetName(code: String): String {
        return setList.firstOrNull{ set -> set.set == code }?.name ?: code
    }

    // Get map of set color assign to set ids
    fun getSetColors(): Map<String, Color> {
        val outputMap = mutableMapOf<String, Color>()
        setList.forEach { set ->
            outputMap.put(set.set, set.color)
        }
        return outputMap
    }

    // Get set color form its code
    fun getSetColor(value: String): Color? {
        var set = setList.firstOrNull{ set -> set.name == value }
        if (set == null) set = setList.firstOrNull{ set -> set.set == value }
        return set?.color
    }

    // Get most probable set
    fun getMostProbableSet(
        rarities: List<String> = listOf()
    ): Pair<Set, Origin>? {
        if (setList.isEmpty()) return null

        var probableSet: Set? = null
        var probableBooster: Origin? = null
        var probableOdd = 0.0f
        setList.forEach{ set ->
            val setOrigin = getMostProbableBooster(set.set, rarities)
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
        setID: String,
        rarities: List<String> = listOf()
    ): Pair<Origin, Float>? {
        val set = setList.find{ set -> set.set == setID }
        if (set == null) return null

        val setOrigins = set.origins.filter{ origin -> OriginsData.getOriginType(origin) == "BOOSTER" }
        if (setOrigins.isEmpty())  return null

        var probableOrigin: Origin? = null
        var probableOdd = 0.0f
        setOrigins.forEach{ origin ->
            val originObject = OriginsData.getOriginByID(origin)
            if (originObject != null) {
                val probabilities = getBoosterRemainingOdds(setID, originObject, rarities)
                var totalProbability = (1 - ((1 - probabilities[0]).pow(3) * (1 - probabilities[1]) * (1 - probabilities[2]))) * 100.0f
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
        val probabilities = mutableListOf(0.0f, 0.0f, 0.0f)

        if (origin.type != "BOOSTER" || origin.odds == null) return probabilities

        val set = setList.find{ set -> set.set == setID }
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
                    for (i in 0 until 3) {
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