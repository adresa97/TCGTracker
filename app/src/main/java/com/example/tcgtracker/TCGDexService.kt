package com.example.tcgtracker

import android.content.Context
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.Set
import com.example.tcgtracker.models.coverMap
import com.example.tcgtracker.models.emptyCover
import com.example.tcgtracker.models.rarityMap
import com.example.tcgtracker.utils.TCGDexGraphQLUtils
import com.google.gson.Gson
import net.tcgdex.sdk.Extension
import net.tcgdex.sdk.Quality
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.models.SetResume

//TODO: Fix Promo Cards Origins

class TCGDexService(language: String) {
    val sdk: TCGdex = TCGdex(language)

    fun getSeriesMap(): Map<String, List<Set>> {
        return sdk.fetchSerie("tcgp")!!.sets.groupBy({set -> extractSeries(set)}, {set ->
            Set(
                extractSeries(set),
                extractExpansion(set),
                set.id,
                set.name,
                coverMap[set.id] ?: emptyCover
            )
        })
    }

    fun getCardsList(set: String): List<Card> {
        val QLRequest = TCGDexGraphQLRequest(
            "Request",
            "query Request {\n  cards(filters: {id: \"${set}-\"}) {\n    id\n    image\n    rarity\n    name\n    types\n    boosters {\n      id\n      name\n    }\n  }\n}"
        )
        val cls = TCGDexGraphQLResponse::class.java
        return TCGDexGraphQLUtils.fetchWithBody(sdk, "${sdk.URI}/graphql", Gson().toJson(QLRequest), cls)!!
            .data.cards.map({ cardData ->
                Card(
                    cardData.id,
                    cardData.name,
                    cardData.types?.elementAtOrNull(0) ?: "",
                    cardData.boosters?.map({ booster -> booster.name }) ?: listOf<String>(),
                    rarityMap[cardData.rarity] ?: "",
                    cardData.getImageUrl(Quality.LOW, Extension.WEBP),
                )
            })
    }

    private fun extractSeries(set: SetResume): String {
        if (isPromo(set)) return set.id.last().toString()
        return set.id.substring(0, 1)
    }

    private fun extractExpansion(set: SetResume): String {
        if (isPromo(set)) return set.id
        return set.id.substring(0, 2)
    }

    private fun isPromo(set: SetResume): Boolean {
        return set.id.startsWith("P-")
    }
}

data class TCGDexCard (
    val id: String,
    val name: String,
    val image: String?,
    val rarity: String,
    val types: List<String>?,
    val boosters: List<TCGDexBooster>?
) {
    fun getImageUrl(quality: Quality, extension: Extension): String {
        return "${this.image}/${quality.value}.${extension.value}"
    }
}

data class TCGDexBooster (
    val id: String,
    val name: String
)

data class TCGDexGraphQLRequest(
    val operationName: String,
    val query: String
)

data class TCGDexGraphQLResponse (
    val data: TCGDexGraphQLCards
)

data class TCGDexGraphQLCards (
    val cards: List<TCGDexCard>
)

