package com.example.tcgtracker.ui.screens.options

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.tcgtracker.models.CardsData
import com.example.tcgtracker.models.DatabaseHandler
import com.example.tcgtracker.models.ExternalJsonSet
import com.example.tcgtracker.models.SQLOwnedCard
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException

object ImporterExporter {
    fun importFromJSON(context: Context, jsonUri: Uri): Pair<String, List<String>?> {
        var jsonString = ""
        if (ContentResolver.SCHEME_CONTENT == jsonUri.scheme) {
            val cr: ContentResolver = context.contentResolver
            val input = cr.openInputStream(jsonUri)
            jsonString = if (input == null) "" else ReadJSONFromFile(input)
        }
        if (jsonString == "") return Pair("ERROR: Archivo vacío", null)

        val type = object : TypeToken<Map<String, ExternalJsonSet>>() {}.type
        val externalData: Map<String, ExternalJsonSet> = Gson().fromJson(jsonString, type)

        val setsList = mutableListOf<String>()
        val dataList = mutableListOf<SQLOwnedCard>()
        externalData.forEach{ entry ->
            val set = if (entry.key == "pA") "P-A" else entry.key
            val cardList = CardsData.getCardList(context, set)
            for (i in 0 until entry.value.values.size) {
                if (cardList.getOrNull(i) != null) {
                    dataList.add(
                        SQLOwnedCard(
                            id = cardList[i].id,
                            set = set,
                            isOwned = entry.value.values[i]
                        )
                    )
                }
            }
            setsList.add(set)
        }

        DatabaseHandler.saveCards(dataList)

        return Pair("Archivo importado con éxito", setsList)
    }

    fun exportToJSON(context: Context, targetFile: Uri): String {
        val jsonData: MutableMap<String, ExternalJsonSet> = mutableMapOf()
        val ownedCards = CardsData.getOwnedCardsMap(context)
        ownedCards.forEach { set->
            jsonData.put(set.key, ExternalJsonSet(set.value))
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