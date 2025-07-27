package com.example.tcgtracker.utils

import com.google.gson.Gson
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.internal.CacheEntry
import net.tcgdex.sdk.internal.Model
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.time.LocalDateTime

object TCGDexGraphQLUtils {
    /**
     * Request Time to Live
     * in minutes
     */
    var ttl: Long = 60

    /**
     * requests cache
     */
    private val cache: HashMap<String, CacheEntry<String>> = HashMap()

    private val gson = Gson()

    /**
     * fetch from the API
     *
     * @param tcgdex the TCGdex instance to link with it
     * @param url the url to fetch from
     * @param cls the Class to build the response into
     *
     * @return an initialized cls or null
     */
    fun <T> fetchWithBody(tcgdex: TCGdex, url: String, body: String, cls: Class<T>): T? {
        var entry = this.cache[getHash(body)]
        val now = LocalDateTime.now().minusMinutes(ttl)
        if (entry == null || entry.time.isBefore(now)) {
            val req = URL(url).openConnection() as HttpURLConnection
            req.setRequestProperty("user-agent", "@tcgdex/java-sdk")

            req.requestMethod = "POST"
            req.setRequestProperty("Content-Type", "application/json")
            req.setRequestProperty("Accept", "application/json")
            req.doInput = true
            req.doOutput = true

            val os = req.outputStream
            val osw = OutputStreamWriter(os, "UTF-8")
            osw.write(body)
            osw.flush()
            osw.close()
            os.close()  //don't forget to close the OutputStream
            req.connect()

            val br = BufferedReader(InputStreamReader(req.getInputStream()));
            var txt = ""
            var line = br.readLine()
            while (line != null)
            {
                txt += line
                line = br.readLine()
            }

            entry = CacheEntry(txt)

            this.cache[getHash(body)] = entry
        }

        try {
            val model = gson.fromJson<T>(
                entry.value, cls
            )
            if (model is Model) {
                model.tcgdex = tcgdex
            }
            return model
        } catch (e: IOException) {
            return null
        }
    }

    fun getHash(text: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(text.toByteArray()).toHexString()
    }
}