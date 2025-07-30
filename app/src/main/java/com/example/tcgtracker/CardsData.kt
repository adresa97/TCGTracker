package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

const val USER_CARDS_DATA_FOLDER_PATH = "PTCGPocket/owned/cards"

class CardsData(private val service: TCGDexService = TCGDexService("en")) {
    private val cardMap: MutableMap<String, MutableList<Card>> = mutableMapOf()
    private var userFolder: File? = null
    private var modified: MutableMap<String, Boolean> = mutableMapOf()

    fun loadJSONSData(applicationContext: Context): Map<String, List<Card>> {
        if(userFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            userFolder = File(extStorageDir, USER_CARDS_DATA_FOLDER_PATH)
            userFolder!!.mkdirs()
        }

        val dirFiles = userFolder?.list()?.asList() ?: listOf()
        if (dirFiles.isEmpty()) return cardMap

        dirFiles.forEach { set->
            val setCode = set.substringAfterLast('/').substringBeforeLast('.')
            if (!cardMap.contains(setCode)) {
                // Retrieve this set owned card list
                val cardList = loadCardList(setCode)

                // Store in class map if card is not empty
                if (cardList.isNotEmpty()) cardMap.put(setCode, cardList)
            }
        }

        return cardMap
    }

    fun loadJSONData(applicationContext: Context, set: String): List<Card> {
        if (cardMap.contains(set)) return cardMap[set] ?: listOf()

        if (userFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            userFolder = File(extStorageDir, USER_CARDS_DATA_FOLDER_PATH)
            userFolder!!.mkdirs()
        }

        try {
            // Retrieve owned card list
            val cardList = loadCardList(set)

            // Store card list in class map and return if card is not empty
            if (cardList.isNotEmpty()) {
                cardMap.put(set, cardList)
                return cardList
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }

        return listOf()
    }

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

    fun updateJSONSData() {
        if (cardMap.isEmpty() || userFolder == null) return

        cardMap.forEach { set->
            updateJSONData(set.key, set.value)
        }
    }

    private fun updateJSONData(set: String, cardList: List<Card>) {
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

    fun getOwnedCardsMap(applicationContext: Context): Map<String, List<Card>> {
        return loadJSONSData(applicationContext)
    }

    fun getCardList(applicationContext: Context, set: String): List<Card> {
        return loadJSONData(applicationContext, set)
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