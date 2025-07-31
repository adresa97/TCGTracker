package com.example.tcgtracker

import com.example.tcgtracker.models.Booster
import com.example.tcgtracker.models.Card
import com.example.tcgtracker.models.CardName
import com.example.tcgtracker.models.InnerJsonSet
import com.example.tcgtracker.models.Rarity
import com.example.tcgtracker.utils.TCGDexGraphQLUtils
import com.google.gson.Gson
import net.tcgdex.sdk.Extension
import net.tcgdex.sdk.Quality
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.models.SetResume

//TODO: Fix Promo Cards Origins

class TCGDexService(language: String) {
    val sdk: TCGdex = TCGdex(language)

    fun getSeriesMap(): Map<String, List<InnerJsonSet>> {
        return sdk.fetchSerie("tcgp")!!.sets.groupBy({set -> extractSeries(set)}, {set ->
            InnerJsonSet(
                series = extractSeries(set),
                expansion = extractExpansion(set),
                set = set.id,
                name = set.name,
                boosters = listOf<String>()
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
                val set = cardData.id.substringBefore('-')
                val boosterList = mutableListOf<Booster>()
                cardData.boosters?.forEach{ booster ->
                    val boosterEnum = Booster.fromPrettyName(booster.name)
                    if (!boosterList.contains(boosterEnum)) boosterList.add(boosterEnum)
                }
                if (boosterList.isEmpty()) boosterList.add(defaultBooster[set] ?: Booster.ERROR)
                val rarity = if (set == "P") Rarity.ERROR else Rarity.fromPrettyName(cardData.rarity)
                Card(
                    id = cardData.id,
                    name = CardName(
                        cardData.name,
                        cardData.name
                    ),
                    type = cardData.types?.elementAtOrNull(0) ?: "",
                    boosters = boosterList,
                    rarity = rarity,
                    image = getImageUrl(cardData.id)
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

    private fun getImageUrl(cardID: String): String {
        var set = cardID.substringBeforeLast('-')
        var number = cardID.substringAfterLast('-').toInt().toString()

        return "https://cdn.pockettrade.app/images/webp/es/${set}_${number}_SPA.webp"
    }
}

val defaultBooster = mutableMapOf<String, Booster>(
    Pair("P", Booster.ERROR),
    Pair("A1", Booster.ERROR),
    Pair("A1a", Booster.MEW),
    Pair("A2", Booster.ERROR),
    Pair("A2a", Booster.ARCEUS),
    Pair("A2b", Booster.SHINY_CHARIZARD),
    Pair("A3", Booster.ERROR),
    Pair("A3a", Booster.BUZZWOLE),
    Pair("A3b", Booster.EEVEE),
    Pair("A4", Booster.ERROR)
)

//region GraphQL data classes
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
//endregion