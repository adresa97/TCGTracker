package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.OWNED_CARDS_FOLDER
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.forEach

class OwnedCardsData(private val service: TCGDexService = TCGDexService("en")) {
    private val ownedCards: MutableMap<String, MutableList<Card>> = mutableMapOf()
    private var directoryFolder: File? = null

    fun loadJSONSData(applicationContext: Context): Map<String, List<Card>> {
        if(directoryFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            directoryFolder = File(extStorageDir, OWNED_CARDS_FOLDER)
        }

        val dirFiles = directoryFolder?.list()?.asList() ?: listOf()
        if (dirFiles.isEmpty()) return ownedCards

        dirFiles.forEach { set->
            val setCode = set.substringAfterLast('/').substringBeforeLast('.')
            if (!ownedCards.contains(setCode)) {
                // Retrieve this set owned card list
                val cardList = loadCardList(setCode)

                // Store in class map if card is not empty
                if (cardList.isNotEmpty()) ownedCards.put(setCode, cardList)
            }
        }

        return ownedCards
    }

    fun loadJSONData(applicationContext: Context, set: String): List<Card> {
        if (ownedCards.contains(set)) return ownedCards[set] ?: listOf()

        if (directoryFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            directoryFolder = File(extStorageDir, OWNED_CARDS_FOLDER)
        }

        try {
            // Retrieve owned card list
            val cardList = loadCardList(set)

            // Store card list in class map and return if card is not empty
            if (cardList.isNotEmpty()) {
                ownedCards.put(set, cardList)
                return cardList
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }

        return listOf()
    }

    private fun loadCardList(set: String): MutableList<Card> {
        // Get this set owned card json
        val file = File(directoryFolder, "${set}.json")
        val jsonString = ReadJSONFromFile(file.inputStream())
        if (jsonString == "") return mutableListOf()

        // Parse json string to data
        val jsonData = Gson().fromJson(jsonString, Array<Boolean>::class.java).asList()

        // Get this set list of cards from service
        val cardList = service.getCardsList(set).toMutableList()
        for (i in 0 until cardList.count()) {
            cardList[i].owned = jsonData[i]
        }

        return cardList
    }

    fun updateJSONSData() {
        if (ownedCards.isEmpty()) return
        if (directoryFolder == null) return

        ownedCards.forEach { set->
            val ownedValues: MutableList<Boolean> = mutableListOf()
            for (i in 0 until set.value.count()) {
                ownedValues.add(i, set.value[i].owned)
            }

            val file = File(directoryFolder, "${set.key}.json")
            val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(ownedValues)

            try {
                val output = FileOutputStream(file)
                output.write(jsonString.toByteArray())
                output.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
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

        val setList = ownedCards[set]
        if (setList == null || cardIndex >= setList.count()) return

        val card = setList[cardIndex]
        card.owned = !card.owned
    }
}