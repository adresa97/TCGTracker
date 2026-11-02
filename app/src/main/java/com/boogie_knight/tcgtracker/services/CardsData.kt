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
    private var activeCollection = ""

    // Load image url constructors from repository
    fun loadImageJSONData() {
        for (game in Routes.Game.entries)
        {
            val jsonString = AssetsRepository.getData(Routes.getRoute(game, Routes.File.IMAGES))
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

                if (cardCollectionMap[game.route] == null) {
                    cardCollectionMap.put(
                        game.route, CardCollection(
                            cardMap = mutableMapOf(),
                            imageSources = sources
                        )
                    )
                } else {
                    cardCollectionMap[game.route]?.imageSources?.putAll(sources)
                }
            }
        }
    }

    fun setActiveCollection(game: Routes.Game) {
        activeCollection = game.route
    }

    fun getActiveCollection(): CardCollection? {
        return cardCollectionMap[activeCollection]
    }

    // Return a set's list of cards
    fun getCardList(set: String): List<Card> {
        val activeCardMap = getActiveCollection()?.cardMap
        if (activeCardMap == null) return listOf()

        val cardList = activeCardMap.getOrElse(set, { loadAssetsJSONData(set).toMutableList() })
        if (cardList.isEmpty()) return listOf()

        val ownedList = loadUserData(cardList)
        if (ownedList.isEmpty()) {
            activeCardMap.put(set, cardList)
            return cardList.toList()
        }

        for (i in 0 until cardList.count()) {
            cardList[i].owned = ownedList.getOrNull(i) ?: false
        }
        activeCardMap.put(set, cardList)
        return cardList.toList()
    }

    // Load user card data for a set
    fun loadUserData(cardList: List<Card>): List<Boolean> {
        val firstPrints = cardList.map{ card -> card.firstPrint }
        val data = UserRepository.getCardsByIds(firstPrints)

        return cardList.map{ card ->
            if (data.containsKey(card.firstPrint)) {
                data[card.firstPrint] ?: false
            } else {
                false
            }
        }
    }

    // Load assets individual JSON data
    fun loadAssetsJSONData(set: String): List<Card> {
        if (activeCollection == "") return emptyList()
        val jsonString = AssetsRepository.getData("${Routes.getRoute(activeCollection, Routes.File.CARDS)}${set}.json")
        return ParseJSON(jsonString, Array<JsonCard>::class.java)?.toList()
            ?.map{ card ->
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    origins = card.origins,
                    rarity = card.rarity,
                    image = getImageUrl(card.firstPrint, card.image),
                    owned = false,
                    extra = card.extra ?: false,
                    firstPrint = card.firstPrint
                )
            } ?: listOf()
    }

    // Load all user data JSONS and then outputs a full map of owned cards values by set
    fun getOwnedCardsMap(): Map<String, List<Boolean>> {
        val output = mutableMapOf<String, List<Boolean>>()

        // Loop through every possible set's lists of cards
        val setList = SetsData.getSetIDs()
        setList.forEach { set ->
            val cardList = getCardList(set)
            val ownedCards = mutableListOf<Boolean>()
            cardList.forEach { card ->
                ownedCards.add(card.owned)
            }
            output.put(set, ownedCards)
        }

        return output
    }

    // Reload user data and update card map
    fun reloadUserData(sets: List<String>) {
        val activeCardMap = getActiveCollection()?.cardMap
        if (activeCardMap == null) return

        sets.forEach{ set ->
            if (activeCardMap.containsKey(set)) {
                val cardList = activeCardMap[set]
                if (cardList != null) {
                    val userData = loadUserData(cardList)
                    if (userData.isNotEmpty()) {
                        for (i in 0 until userData.count()) {
                            getActiveCollection()!!.cardMap[set]!![i].owned = userData[i]
                        }
                    }
                }
            }
        }
    }

    private fun getImageUrl(cardID: String, urlCode: String?): String {
        val code = urlCode ?: "v1"
        val source = getActiveCollection()?.imageSources[code]
        if (source == null) return ""

        val set = cardID.substringBeforeLast('-')
        val number = cardID.substringAfterLast('-')
        val single = number.toInt().toString()

        var lang2 = "es"
        var lang3 = "SPA"

        if (activeCollection == Routes.Game.ALYSION.route) {
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

    fun changeCardState(set: String, cardIndex: Int) {
        if (cardIndex < 0) return

        val activeCardMap = getActiveCollection()?.cardMap
        if (activeCardMap == null) return

        val setList = activeCardMap[set]
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
                    set = originalSet,
                    isOwned = isNowOwned
                )
            )
        )
    }
}