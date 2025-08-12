package com.example.tcgtracker.utils

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun ReadJSONFromAssets(context: Context, path:String): String {
    return ReadJSONFromFile(context.assets.open(path))
}

fun ReadJSONFromFile(file: InputStream): String {
    val identifier = "[ReadJSON]"
    try {
        Log.i(
            identifier,
            "Found file: $file.",
        )

        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
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