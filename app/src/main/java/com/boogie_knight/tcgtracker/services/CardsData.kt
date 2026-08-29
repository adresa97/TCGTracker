package com.boogie_knight.tcgtracker.services

import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.models.Card
import com.boogie_knight.tcgtracker.models.ImageURLData
import com.boogie_knight.tcgtracker.models.JsonCard
import com.boogie_knight.tcgtracker.models.JsonImageURL
import com.boogie_knight.tcgtracker.models.SQLOwnedCard
import com.boogie_knight.tcgtracker.repositories.UserRepository
import com.boogie_knight.tcgtracker.utils.ParseJSON

const val ASSETS_CARDS_DATA_FOLDER_PATH = "PTCGPocket/cards"
const val ASSETS_IMAGE_SOURCES_DATA_FILE_PATH = "PTCGPocket/imageSources.json"

object CardsData {
    private val cardMap: MutableMap<String, MutableList<Card>> = mutableMapOf()
    private val imageSources: MutableMap<String, ImageURLData> = mutableMapOf()

    // Load image url constructors from repository
    fun loadImageJSONData() {
        val jsonString = AssetsRepository.getData(ASSETS_IMAGE_SOURCES_DATA_FILE_PATH)
        val jsonData = ParseJSON(jsonString, Array<JsonImageURL>::class.java)?.asList()
        jsonData?.forEach { source ->
            imageSources.put(source.code, ImageURLData(
                source.host,
                source.folder,
                source.file
            ))
        }
    }

    // Return a set's list of cards
    fun getCardList(set: String): List<Card> {
        val cardList = cardMap.getOrElse(set, { loadAssetsJSONData(set).toMutableList() })
        if (cardList.isEmpty()) return listOf()

        val ownedList = loadUserData(cardList)
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
        val jsonString = AssetsRepository.getData("${ASSETS_CARDS_DATA_FOLDER_PATH}/${set}.json")
        return ParseJSON(jsonString, Array<JsonCard>::class.java)?.toList()
            ?.map{ card ->
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    origins = card.origins,
                    rarity = card.rarity,
                    image = getImageUrl(card.id, card.image),
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
        sets.forEach{ set ->
            if (cardMap.containsKey(set)) {
                val cardList = cardMap[set]
                if (cardList != null) {
                    val userData = loadUserData(cardList)
                    if (userData.isNotEmpty()) {
                        for (i in 0 until userData.count()) {
                            cardMap[set]!![i].owned = userData[i]
                        }
                    }
                }
            }
        }
    }

    private fun getImageUrl(cardID: String, urlCode: String?): String {
        val code = urlCode ?: "v1"
        val source = imageSources[code]
        if (source == null) return ""

        val set = cardID.substringBeforeLast('-')
        val number = cardID.substringAfterLast('-')
        val single = number.toInt().toString()
        val lang2 = "es"
        val lang3 = "SPA"

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
                    set = originalSet,
                    isOwned = isNowOwned
                )
            )
        )
    }
}