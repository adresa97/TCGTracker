package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Booster
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.CoverImage
import com.example.tcgtracker.models.InnerJsonSet
import com.example.tcgtracker.models.OwnedData
import com.example.tcgtracker.models.OwnedSetData
import com.example.tcgtracker.models.Rarity
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.models.UserJsonOwnedSetData
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

            // Get boosters enum list
            val boosters = mutableListOf<Booster>()
            set.boosters.forEach { booster ->
                boosters.add(Booster.fromPrettyName(booster))
            }

            // Add this set entry to collections.
            // If calculated numbers aren't stored in user data, calculate them from cards data
            setList.add(Set(
                series = set.series,
                expansion = set.expansion,
                set = set.set,
                name = set.name,
                cover = cover,
                boosters = boosters,
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

        val boosters = mutableListOf<Booster>()
        cardList.forEach { card ->
            val cardBoosters = card.booster
            if (cardBoosters.isNotEmpty()) {
                val mainBooster = cardBoosters[0]
                if (!boosters.contains(mainBooster)) boosters.add(mainBooster)
            }
        }
        val byBooster = mutableMapOf<Booster, OwnedData>()
        boosters.forEach { booster ->
            val boosterCards = cardList.stream().filter{ card -> card.booster.contains(booster) }.toArray().asList() as List<Card>
            val data = OwnedData(
                totalCards = boosterCards.count(),
                ownedCards = boosterCards.stream().filter{ card -> card.owned }.count().toInt()
            )
            byBooster.put(booster, data)
        }

        val byRarity = mutableMapOf<Rarity, OwnedData>()
        Rarity.entries.forEach { rarity ->
            val rarityCards = cardList.stream().filter{ card -> card.rarity == rarity }.toArray().asList() as List<Card>
            val data = OwnedData(
                totalCards = rarityCards.count(),
                ownedCards = rarityCards.stream().filter{ card -> card.owned }.count().toInt()
            )
            byRarity.put(rarity, data)
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
}