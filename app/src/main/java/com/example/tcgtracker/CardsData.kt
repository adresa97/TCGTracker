package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.InnerJsonCard
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val ASSETS_CARDS_DATA_FOLDER_PATH = "PTCGPocket/cards"
const val USER_CARDS_DATA_FOLDER_PATH = "PTCGPocket/owned/cards"

object CardsData {
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
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    origins = card.origins,
                    rarity = card.rarity,
                    image = getImageUrl(card.id),
                    owned = false,
                    baby = card.baby
                )
            }
    }

    // Load all user data JSONS and then outputs a full map of owned cards values by set
    fun getOwnedCardsMap(context: Context): Map<String, List<Boolean>> {
        val output = mutableMapOf<String, List<Boolean>>()

        // Loop through every possible set's lists of cards
        val setList = SetsData.getSetIDs()
        setList.forEach { set ->
            val cardList = getCardList(context, set)
            val ownedCards = mutableListOf<Boolean>()
            cardList.forEach{ card ->
                ownedCards.add(card.owned)
            }
            output.put(set, ownedCards)
        }

        return output
    }

    // Reload user JSONS data and update card map
    fun reloadUserJSONSData(applicationContext: Context, sets: List<String>) {
        sets.forEach{ set ->
            if (cardMap.containsKey(set)) {
                val userData = loadUserJSONData(applicationContext, set)
                if (userData.isNotEmpty()) {
                    for (i in 0 until userData.count()) {
                        cardMap[set]!![i].owned = userData[i]
                    }
                }
            }
        }
    }

    private fun getImageUrl(cardID: String): String {
        val set = cardID.substringBeforeLast('-')
        val number = cardID.substringAfterLast('-').toInt().toString()

        return "https://cdn.pockettrade.app/images/webp/es/${set}_${number}_SPA.webp"
    }

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