package com.boogie_knight.tcgtracker.services

import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.Card
import com.boogie_knight.tcgtracker.models.ImageURLData
import com.boogie_knight.tcgtracker.models.JsonCard
import com.boogie_knight.tcgtracker.models.JsonImageURL
import com.boogie_knight.tcgtracker.models.SQLOwnedCard
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.utils.ParseJSON

object CardsData {
    data class CardCollection (
        val cardMap: MutableMap<String, MutableList<Card>>,
        val imageSources: MutableMap<String, ImageURLData>
    )

    private val cardCollectionMap = mutableMapOf<String, CardCollection>()

    // Load image url constructors from repository
    fun loadImageJSONData() {
        val games = GamesData.getGamesList()
        for (game in games)
        {
            val jsonString = AssetsRepository.getData(GamesData.getGameImageSourcesRoute(game))
            val jsonData = ParseJSON(jsonString, Array<JsonImageURL>::class.java)?.asList()

            if (jsonData != null)
            {
                val sources = mutableMapOf<String, ImageURLData>()
                jsonData.forEach { source ->
                    sources.put(source.code, ImageURLData(
                        source.host,
                        source.folder,
                        source.file
                    ))
                }

                if (cardCollectionMap[game] == null) {
                    cardCollectionMap.put(
                        game, CardCollection(
                            cardMap = mutableMapOf(),
                            imageSources = sources
                        )
                    )
                } else {
                    cardCollectionMap[game]?.imageSources?.putAll(sources)
                }
            }
        }
    }

    fun getGameCollection(game: String): CardCollection? {
        return cardCollectionMap[game]
    }

    fun getGlobalCardMap(): Map<String, List<Card>> {
        val cardMap = mutableMapOf<String, List<Card>>()
        cardCollectionMap.forEach{ game ->
            cardMap.putAll(game.value.cardMap)
        }
        return cardMap
    }

    // Return a set's list of cards
    fun getCardList(game: String, set: String): List<Card> {
        val cardMap = getGameCollection(game)?.cardMap
        if (cardMap == null) return listOf()

        val cardList = cardMap.getOrElse(set, { loadAssetsJSONData(game, set).toMutableList() })
        if (cardList.isEmpty()) return listOf()

        val ownedList = loadUserData(game, cardList)
        if (ownedList.isEmpty()) {
            cardMap.put(set, cardList)
            return cardList.toList()
        }

        for (i in 0 until cardList.count()) {
            cardList[i].owned = ownedList.getOrNull(i) ?: false
        }
        cardMap.put(set, cardList)
        return cardList.toList()
    }

    // Load user card data for a set
    fun loadUserData(game: String, cardList: List<Card>): List<Boolean> {
        val firstPrints = cardList.map{ card -> card.firstPrint }
        val data = UserRepository.getCardsByIds(game = game, ids = firstPrints)

        return cardList.map{ card ->
            if (data.containsKey(card.firstPrint)) {
                data[card.firstPrint] ?: false
            } else {
                false
            }
        }
    }

    // Load assets individual JSON data
    fun loadAssetsJSONData(game: String, set: String): List<Card> {
        val jsonString = AssetsRepository.getData("${GamesData.getGameCardsFolderRoute(game)}${set}.json")
        return ParseJSON(jsonString, Array<JsonCard>::class.java)?.toList()
            ?.map{ card ->
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    origins = card.origins,
                    rarity = card.rarity,
                    image = getImageUrl(game, card.firstPrint, card.image),
                    owned = false,
                    extra = card.extra ?: false,
                    firstPrint = card.firstPrint
                )
            } ?: listOf()
    }

    // Load all user data JSONS and then outputs a full map of owned cards values by set
    fun getOwnedCardsMap(game: String): Map<String, List<Boolean>> {
        val output = mutableMapOf<String, List<Boolean>>()

        // Loop through every possible set's lists of cards
        val setList = SetsData.getSetIDs(game)
        setList.forEach { set ->
            val cardList = getCardList(game, set)
            val ownedCards = mutableListOf<Boolean>()
            cardList.forEach { card ->
                ownedCards.add(card.owned)
            }
            output.put(set, ownedCards)
        }

        return output
    }

    // Reload user data and update card map
    fun reloadUserData(game: String, sets: List<String>) {
        val cardMap = getGameCollection(game)?.cardMap
        if (cardMap == null) return

        sets.forEach{ set ->
            if (cardMap.containsKey(set)) {
                val cardList = cardMap[set]
                if (cardList != null) {
                    val userData = loadUserData(game, cardList)
                    if (userData.isNotEmpty()) {
                        for (i in 0 until userData.count()) {
                            getGameCollection(game)!!.cardMap[set]!![i].owned = userData[i]
                        }
                    }
                }
            }
        }
    }

    private fun getImageUrl(game: String, cardID: String, urlCode: String?): String {
        val code = urlCode ?: "v1"
        val source = getGameCollection(game)?.imageSources[code]
        if (source == null) return ""

        val set = cardID.substringBeforeLast('-')
        val number = cardID.substringAfterLast('-')
        val single = number.toInt().toString()

        var lang2 = "es"
        var lang3 = "SPA"

        if (game == "ALYSION") {
            lang2 = "en"
            lang3 = "ENG"
        }

        val url = "${source.host}${source.folder}${source.file}"
            .replace("#set#", set)
            .replace("#number#", number)
            .replace("#single#", single)
            .replace("#lang2#", lang2)
            .replace("#lang3#", lang3)
        return url
    }

    fun changeCardState(game: String, set: String, cardIndex: Int) {
        if (cardIndex < 0) return

        val cardMap = getGameCollection(game)?.cardMap
        if (cardMap == null) return

        val setList = cardMap[set]
        if (setList == null || cardIndex >= setList.count()) return

        val card = setList[cardIndex]
        val isNowOwned = !card.owned
        card.owned = isNowOwned

        val originalPrint = card.firstPrint
        val originalSet = originalPrint.substringBeforeLast('-')
        UserRepository.saveCards(
            cards =listOf(
                SQLOwnedCard(
                    id = originalPrint,
                    game = game,
                    set = originalSet,
                    isOwned = isNowOwned
                )
            )
        )
    }
}