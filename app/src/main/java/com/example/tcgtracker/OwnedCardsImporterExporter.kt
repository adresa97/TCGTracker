package com.example.tcgtracker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.tcgtracker.models.ExternalJsonSet
import com.example.tcgtracker.utils.ReadJSONFromFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object OwnedCardsImporterExporter {
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

        val extStorageDir = context.getExternalFilesDir(null)
        val folder = File(extStorageDir, USER_CARDS_DATA_FOLDER_PATH)
        folder.mkdirs()

        val setsList = mutableListOf<String>()
        externalData.forEach { set ->
            val key = if (set.key == "pA") "P-A" else set.key
            val file = File(folder, "${key}.json")
            val setString = GsonBuilder().setPrettyPrinting().create().toJson(set.value.values)

            try {
                val output = FileOutputStream(file)
                output.write(setString.toByteArray())
                output.close()
                setsList.add(key)
            } catch (e: IOException) {
                e.printStackTrace()
                return Pair("ERROR: Error al crear/actualizar ${key}.json", null)
            }
        }

        return Pair("Archivo importado con éxito", setsList)
    }

    fun exportToJSON(context: Context, ownedCards: Map<String, List<Boolean>>, targetFolder: File, filename: String): String {
        val jsonData: MutableMap<String, ExternalJsonSet> = mutableMapOf()
        ownedCards.forEach { set->
            jsonData.put(set.key, ExternalJsonSet(set.value))
        }

        val file = File(targetFolder, "${filename}.json")
        val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(jsonData)

        try {
            val output = FileOutputStream(file)
            output.write(jsonString.toByteArray())
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return "ERROR: Error al crear el archivo"
        }

        return "Archivo exportado con éxito en: ${targetFolder.absolutePath}"
    }
}

