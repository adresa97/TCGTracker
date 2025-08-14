package com.boogie_knight.tcgtracker.utils

import java.io.IOException
import java.net.URL

object NetworkFetchUtils {
    fun fetch(url: String): String {
        try {
            val req = URL(url).openConnection()
            return ReadJSONFromFile(req.getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}