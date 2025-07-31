package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Booster
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.InnerJsonCard
import com.example.tcgtracker.models.Rarity
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val ASSETS_CARDS_DATA_FOLDER_PATH = "PTCGPocket/cards"
const val USER_CARDS_DATA_FOLDER_PATH = "PTCGPocket/owned/cards"

class CardsData(private val service: TCGDexService = TCGDexService("en")) {
    private val cardMap: MutableMap<String, MutableList<Card>> = mutableMapOf()
    private var userFolder: File? = null
    private var modified: MutableMap<String, Boolean> = mutableMapOf()

    // Return a set's list of cards
    fun getCardList(applicationContext: Context, set: String): List<Card> {
        if (cardMap.contains(set)) return cardMap[set]!!.toList()

        val cardList = loadAssetsJSONData(applicationContext, set).toMutableList()
        if (cardList.isEmpty()) return listOf()

        val ownedList = loadUserJSONData(applicationContext, set)
        if (ownedList.isEmpty()) {
            cardMap.put(set, cardList)
            return cardList.toList()
        }

        for (i in 0 until cardList.count()) {
            cardList[i].owned = ownedList.getOrNull(i) ?: false
        }
        cardMap.put(set, cardList)
        return cardList.toList()
    }

    // Load user individual JSON data
    fun loadUserJSONData(applicationContext: Context, set: String): List<Boolean> {
        if (userFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            userFolder = File(extStorageDir, USER_CARDS_DATA_FOLDER_PATH)
            userFolder!!.mkdirs()
        }

        val file = File(userFolder, "${set}.json")
        if (!file.exists()) return listOf()

        val jsonString = ReadJSONFromFile(file.inputStream())
        return Gson().fromJson(jsonString, Array<Boolean>::class.java).toList()
    }

    // Load assets individual JSON data
    fun loadAssetsJSONData(applicationContext: Context, set: String): List<Card> {
        val jsonString = ReadJSONFromAssets(applicationContext, "${ASSETS_CARDS_DATA_FOLDER_PATH}/${set}.json")
        return Gson().fromJson(jsonString, Array<InnerJsonCard>::class.java).toList()
            .map{ card ->
                val boosters = mutableListOf<Booster>()
                card.boosters.forEach { pack ->
                    boosters.add(Booster.fromPrettyName(pack))
                }
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    boosters = boosters,
                    rarity = Rarity.fromPrettyName(card.rarity),
                    image = getImageUrl(card.id),
                    owned = false,
                    baby = card.baby
                )
            }
    }

    private fun getImageUrl(cardID: String): String {
        var set = cardID.substringBeforeLast('-')
        var number = cardID.substringAfterLast('-').toInt().toString()

        return "https://cdn.pockettrade.app/images/webp/es/${set}_${number}_SPA.webp"
    }

    /*
    private fun loadCardList(set: String): MutableList<Card> {
        // Get this set list of cards from service
        val cardList = service.getCardsList(set).toMutableList()

        // Try to get this set owned card user json
        var jsonData: List<Boolean> = mutableListOf()
        try {
            // Get json
            val file = File(userFolder, "${set}.json")
            val jsonString = ReadJSONFromFile(file.inputStream())
            if (jsonString == "") return mutableListOf()

            // Parse json string to data
            jsonData = Gson().fromJson(jsonString, Array<Boolean>::class.java).asList()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // If json doesn't exist or is empty
        if (jsonData.isEmpty()) return cardList

        // Update cards owned value according to json data
        for (i in 0 until cardList.count()) {
            cardList[i].owned = if (i < jsonData.count()) jsonData[i] else false
        }

        return cardList
    }
     */

    fun updateUserJSONSData() {
        if (cardMap.isEmpty() || userFolder == null) return

        cardMap.forEach { set->
            updateUserJSONData(set.key, set.value)
        }
    }

    private fun updateUserJSONData(set: String, cardList: List<Card>) {
        if (!(modified[set] ?: false)) return

        val ownedValues: MutableList<Boolean> = mutableListOf()
        for (i in 0 until cardList.count()) {
            ownedValues.add(i, cardList[i].owned)
        }

        val file = File(userFolder, "${set}.json")
        val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(ownedValues)

        try {
            val output = FileOutputStream(file)
            output.write(jsonString.toByteArray())
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun changeCardState(set: String, cardIndex: Int) {
        if (cardIndex < 0) return

        val setList = cardMap[set]
        if (setList == null || cardIndex >= setList.count()) return

        val card = setList[cardIndex]
        card.owned = !card.owned

        if (modified.containsKey(set)) modified[set] = true
        else modified.put(set, true)
    }
}