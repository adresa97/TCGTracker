package com.boogie_knight.tcgtracker.ui.screens.options

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.boogie_knight.tcgtracker.POCKET_GAME_KEY
import com.boogie_knight.tcgtracker.services.CardsData
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.models.ExternalJsonSet
import com.boogie_knight.tcgtracker.models.SQLOwnedCard
import com.boogie_knight.tcgtracker.services.GamesData
import com.boogie_knight.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

object ImporterExporter {
    fun importFromJSON(context: Context, jsonUri: Uri): Pair<String, Map<String, List<String>>?> {
        var jsonString = ""
        if (ContentResolver.SCHEME_CONTENT == jsonUri.scheme) {
            val cr: ContentResolver = context.contentResolver
            val input = cr.openInputStream(jsonUri)
            jsonString = if (input == null) "" else ReadJSONFromFile(input)
        }
        if (jsonString == "") return Pair("ERROR: Archivo vacío", null)

        val typeV1 = object : TypeToken<Map<String, ExternalJsonSet>>() {}.type
        val typeV2 = object : TypeToken<Map<String, Map<String, ExternalJsonSet>>>() {}.type
        var externalData: Map<String, Map<String, ExternalJsonSet>>
        try {
            externalData = Gson().fromJson(jsonString, typeV2)
        } catch(_: JsonSyntaxException) {
            externalData = mapOf(Pair(POCKET_GAME_KEY, Gson().fromJson(jsonString, typeV1)))
        }

        val gameMap = mutableMapOf<String, List<String>>()
        val dataList = mutableListOf<SQLOwnedCard>()
        externalData.forEach{ entry->
            if (GamesData.isGameInData(entry.key)) {
                val setsList = getSetFromJson(entry.key, dataList, entry.value)
                gameMap.put(entry.key, setsList)
            }
        }

        UserRepository.saveCards(dataList)

        return Pair("Archivo importado con éxito", gameMap)
    }

    fun getSetFromJson(game: String, dataList: MutableList<SQLOwnedCard>, externalData: Map<String, ExternalJsonSet>): List<String> {
        val setsList = mutableListOf<String>()
        externalData.forEach { entry ->
            val set = if (entry.key == "pA") "P-A" else entry.key
            val cardList = CardsData.getCardList(game, set)
            for (i in 0 until entry.value.values.size) {
                if (cardList.getOrNull(i) != null) {
                    val firstPrint = cardList[i].firstPrint
                    val printSet = firstPrint.substringBeforeLast('-')
                    var isOwned = entry.value.values[i]
                    if (firstPrint != cardList[i].id) {
                        val externalDataSet = if (printSet == "P-A") "pA" else printSet
                        if (externalData[externalDataSet] != null) {
                            val externalIndex = firstPrint.substringAfterLast('-').toIntOrNull()
                            if (externalIndex != null) isOwned = externalData[externalDataSet]!!.values[externalIndex]
                        }
                    }
                    dataList.add(
                        SQLOwnedCard(
                            game = game,
                            id = firstPrint,
                            set = printSet,
                            isOwned = isOwned
                        )
                    )
                }
            }
            setsList.add(set)
        }
        return setsList
    }

    fun exportToJSON(context: Context, targetFile: Uri): String {
        val jsonData: MutableMap<String, Map<String, ExternalJsonSet>> = mutableMapOf()
        GamesData.getGamesList().forEach{ game->
            val ownedCards = CardsData.getOwnedCardsMap(game)
            if (!ownedCards.isEmpty()) {
                val jsonSet = mutableMapOf<String, ExternalJsonSet>()
                ownedCards.forEach{ set ->
                    jsonSet.put(set.key,ExternalJsonSet(set.value))
                }
                jsonData.put(game, jsonSet)
            }
        }

        val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(jsonData)

        try {
            context.contentResolver.openOutputStream(targetFile).use{ stream ->
                stream?.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "ERROR: Error al crear el archivo"
        }

        return "Archivo exportado con éxito en: ${targetFile.path}"
    }
}