package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.OWNED_CARDS_FOLDER
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.forEach

class OwnedCardsData() {
    private val ownedCards: MutableMap<String, MutableList<Boolean>> = mutableMapOf()
    private var directoryFolder: File? = null

    fun loadJSONSData(applicationContext: Context): Map<String, List<Boolean>> {
        if(directoryFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            directoryFolder = File(extStorageDir, OWNED_CARDS_FOLDER)
        }

        val dirFiles = directoryFolder?.list()

        if (dirFiles == null) return mapOf()
        if (ownedCards.isNotEmpty() && ownedCards.count() == dirFiles.count()) return ownedCards

        dirFiles.forEach { set->
            val setCode = set.substringAfterLast('/').substringBeforeLast('.')
            if (!ownedCards.contains(setCode)) {
                val file = File(set)
                val jsonString = ReadJSONFromFile(file.inputStream())
                val jsonData = Gson().fromJson(jsonString, Array<Boolean>::class.java).asList()
                ownedCards.put(setCode, jsonData.toMutableList())
            }
        }
        return ownedCards
    }

    fun loadJSONData(applicationContext: Context, set: String): List<Boolean> {
        if (ownedCards.contains(set)) return ownedCards[set] ?: listOf()

        if (directoryFolder == null) {
            val extStorageDir = applicationContext.getExternalFilesDir(null)
            directoryFolder = File(extStorageDir, OWNED_CARDS_FOLDER)
        }

        try {
            val file = File(directoryFolder, "${set}.json")
            val jsonString = ReadJSONFromFile(file.inputStream())
            val jsonData = Gson().fromJson(jsonString, Array<Boolean>::class.java).asList()
            ownedCards.put(set, jsonData.toMutableList())
            return jsonData
        } catch(e: IOException) {
            e.printStackTrace()
        }

        return listOf()
    }

    fun updateJSONSData() {
        if (ownedCards.isEmpty()) return
        if (directoryFolder == null) return

        ownedCards.forEach { set->
            val file = File(directoryFolder, "${set.key}.json")
            val jsonString = Gson().toJson(set.value)

            try {
                val output = FileOutputStream(file)
                output.write(jsonString.toByteArray())
                output.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getOwnedCardsMap(applicationContext: Context): Map<String, List<Boolean>> {
        return loadJSONSData(applicationContext)
    }

    fun getCardList(applicationContext: Context, set: String): List<Boolean> {
        return loadJSONData(applicationContext, set)
    }

    fun changeCardState(set: String, cardIndex: Int) {
        if (cardIndex < 0) return

        val setList = ownedCards[set]
        if (setList != null && cardIndex < setList.count()) {
            ownedCards[set]!![cardIndex] = !setList[cardIndex]
        }
    }
}