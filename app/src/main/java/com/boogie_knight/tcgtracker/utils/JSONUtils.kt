package com.boogie_knight.tcgtracker.utils

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

fun ReadJSONFromFile(file: InputStream): String {
    val identifier = "[ReadJSON]"
    try {
        Log.i(
            identifier,
            "Found file: $file.",
        )

        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { line ->
            stringBuilder.append(line)
        }
        Log.i(
            identifier,
            "getJSON stringBuilder: $stringBuilder.",
        )

        val jsonString = stringBuilder.toString()
        Log.i(
            identifier,
            "JSON as String: $jsonString.",
        )
        return jsonString
    } catch (e: Exception) {
        Log.e(
            identifier,
            "Error reading JSON: $e.",
        )
        e.printStackTrace()
        return ""
    }
}

private val gson = Gson()
fun <T> ParseJSON(json: String, cls: Class<T>): T? {
    try {
        val model = gson.fromJson<T>(
            json, cls
        )
        return model
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}