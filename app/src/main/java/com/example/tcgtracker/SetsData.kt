package com.example.tcgtracker

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.CoverImage
import com.example.tcgtracker.models.InnerJsonSet
import com.example.tcgtracker.models.Origin
import com.example.tcgtracker.models.OwnedData
import com.example.tcgtracker.models.OwnedSetData
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.pow

const val ASSETS_SETS_DATA_FILE_PATH = "PTCGPocket/sets.json"
const val USER_SETS_DATA_FILE_PATH = "PTCGPocket/owned/sets.json"

class SetsData() {
    private val setList = mutableListOf<Set>()
    private var userFile: File? = null
    private var modified: Boolean = false

    // Populate collections list
    fun loadJSONData(applicationContext: Context, cardsData: CardsData) {
        // If data already stored end function
        if (!setList.isEmpty()) return

        // Get inner data (objective data of every set)
        val innerJsonString = ReadJSONFromAssets(applicationContext, ASSETS_SETS_DATA_FILE_PATH)
        val innerJsonData = Gson().fromJson(innerJsonString, Array<InnerJsonSet>::class.java).asList()

        // If user data file not located, find it
        if(userFile == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            userFile = File(extStorageDir, USER_SETS_DATA_FILE_PATH)
            userFile!!.parentFile!!.mkdirs()
        }

        // If existing, get user data (subjective data of every set)
        var userJsonData: Map<String, OwnedSetData> = mutableMapOf()
        try {
            val file = File(userFile, "sets.json")
            val userJsonString = ReadJSONFromFile(file.inputStream())
            val type = object : TypeToken<Map<String, OwnedSetData>>() {}.type
            userJsonData = Gson().fromJson(userJsonString, type)
        } catch(e: IOException) {
            e.printStackTrace()
        }

        // Map both data into collections
        innerJsonData.forEach { set ->
            // Get cover enum from set code
            val cover = CoverImage.from(set.set)

            // Turn set color string to Color object
            val color = Color(set.color[0]/255, set.color[1]/255, set.color[2]/255)

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
                numbers = userJsonData[set.set] ?: calculateNumbers(
                    cardList = cardsData.getCardList(applicationContext, set.set)
                )
            ))
        }
    }

    // Update user data JSON to keep changes
    fun updateUserJSON() {
        // If user data file is not located then there's no data to store
        if (setList.isEmpty() || userFile == null || !modified) return

        // Get numbers from all sets
        val allSetsNumbers = mutableMapOf<String, OwnedSetData>()
        setList.forEach { set ->
            allSetsNumbers.put(set.set, set.numbers)
        }

        // Turn map to json strings
        val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(allSetsNumbers)

        // Try to create or overwrite json file
        try {
            val output = FileOutputStream(userFile)
            output.write(jsonString.toByteArray())
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Calculate all needed numbers from a list of cards and boosters
    private fun calculateNumbers(cardList: List<Card>): OwnedSetData {
        val all = OwnedData(
            totalCards = cardList.count(),
            ownedCards = cardList.stream().filter{ card -> card.owned }.count().toInt()
        )

        val byRarity = mutableMapOf<String, OwnedData>()
        Concepts.getRarities().forEach { rarity ->
            val rarityCards = cardList.stream().filter{ card -> card.rarity == rarity }.toArray().asList() as List<Card>
            val data = OwnedData(
                totalCards = rarityCards.count(),
                ownedCards = rarityCards.stream().filter{ card -> card.owned }.count().toInt()
            )
            byRarity.put(rarity, data)
        }

        val boosters = mutableListOf<String>()
        cardList.forEach { card ->
            val cardOrigins = card.origins
            if (!cardOrigins.isEmpty()) {
                val mainOrigin = cardOrigins[0]
                if (!boosters.contains(mainOrigin)) boosters.add(mainOrigin)
            }
        }
        val byBooster = mutableMapOf<String, Map<String, OwnedData>>()
        boosters.forEach { booster ->
            val boosterCards = cardList.stream().filter{ card -> card.origins.contains(booster) }.toArray().asList() as List<Card>
            Concepts.getRarities().forEach { rarity ->
                val rarityCards = boosterCards.stream().filter{ card -> card.rarity == rarity }.toArray().asList() as List<Card>
                val data = OwnedData(
                    totalCards = rarityCards.count(),
                    ownedCards = rarityCards.stream().filter{ card -> card.owned }.count().toInt()
                )
                val byRarity = mutableMapOf<String, OwnedData>(Pair(rarity, data))
                byBooster.put(booster, byRarity)
            }
        }

        return OwnedSetData(all = all, byBooster = byBooster, byRarity = byRarity)
    }

    fun recalculateSetData(cardList: List<Card>, setCode: String) {
        val newCalculations = calculateNumbers(cardList)
        setList.firstOrNull{ set -> set.set == setCode }?.numbers = newCalculations

        modified = true
    }

    // Get all sets of a series
    fun getSeriesMap(): Map<String, List<Set>> {
        return setList.groupBy({ set -> set.series })
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
        originsData: OriginsData,
        rarities: List<String> = listOf()
    ): Pair<Set, Origin>? {
        if (setList.isEmpty()) return null

        var probableSet: Set? = null
        var probableBooster: Origin? = null
        var probableOdd = 0.0f
        setList.forEach{ set ->
            val setOrigin = getMostProbableBooster(set.set, originsData, rarities)
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
        originsData: OriginsData,
        rarities: List<String> = listOf()
    ): Pair<Origin, Float>? {
        val set = setList.find{ set -> set.set == setID }
        if (set == null) return null

        val setOrigins = set.origins.filter{ origin -> originsData.getOriginType(origin) == "BOOSTER" }
        if (setOrigins.isEmpty())  return null

        var probableOrigin: Origin? = null
        var probableOdd = 0.0f
        setOrigins.forEach{ origin ->
            val originObject = originsData.getOriginByID(origin)
            if (originObject != null) {
                val probabilities = getBoosterRemainingOdds(setID, originObject, rarities)
                var totalProbability = 1 - ((1 - probabilities[0]).pow(3) * (1 - probabilities[1]) * (1 - probabilities[2]))
                if (totalProbability > 100.0f) totalProbability = 100.0f
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

        val numbers = set.numbers.byBooster[origin.id]
        if (numbers == null) return probabilities

        val fixedRarities = rarities.ifEmpty { origin.odds.keys.toList() }

        origin.odds.forEach{ rarity ->
            if (numbers.containsKey(rarity.key) && fixedRarities.contains(rarity.key)) {
                val remainingCards = numbers[rarity.key]!!.totalCards - numbers[rarity.key]!!.ownedCards
                for (i in 0 until 3) {
                    probabilities[i] += remainingCards * rarity.value[i]
                    if (probabilities[i] > 100.0f) probabilities[i] = 100.0f
                    else if (probabilities[i] < 0.0f) probabilities[i] = 0.0f
                }
            }
        }

        return probabilities
    }
}