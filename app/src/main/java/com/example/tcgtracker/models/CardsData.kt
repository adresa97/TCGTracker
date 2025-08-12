package com.example.tcgtracker.models

import android.content.Context
import com.example.tcgtracker.utils.ReadJSONFromAssets
import com.google.gson.Gson

const val ASSETS_CARDS_DATA_FOLDER_PATH = "PTCGPocket/cards"

object CardsData {
    private val cardMap: MutableMap<String, MutableList<Card>> = mutableMapOf()

    // Return a set's list of cards
    fun getCardList(applicationContext: Context, handler: SQLiteHandler, set: String): List<Card> {
        if (cardMap.contains(set)) return cardMap[set]!!.toList()

        val cardList = loadAssetsJSONData(applicationContext, set).toMutableList()
        if (cardList.isEmpty()) return listOf()

        val ownedList = loadUserData(handler, set, cardList)
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
    fun loadUserData(handler: SQLiteHandler, set: String, cardList: List<Card>): List<Boolean> {
        val data = handler.getCardsBySet(set)
        val outList = mutableListOf<Boolean>()
        cardList.forEach{ card ->
            if (data.containsKey(card.id)) {
                outList.add(data[card.id] ?: false)
            } else {
                outList.add(false)
            }
        }

        return outList
    }

    // Load assets individual JSON data
    fun loadAssetsJSONData(applicationContext: Context, set: String): List<Card> {
        val jsonString = ReadJSONFromAssets(applicationContext, "${ASSETS_CARDS_DATA_FOLDER_PATH}/${set}.json")
        return Gson().fromJson(jsonString, Array<InnerJsonCard>::class.java).toList()
            .map{ card ->
                Card(
                    id = card.id,
                    name = card.name,
                    type = card.type,
                    origins = card.origins,
                    rarity = card.rarity,
                    image = getImageUrl(card.id),
                    owned = false,
                    baby = card.baby
                )
            }
    }

    // Load all user data JSONS and then outputs a full map of owned cards values by set
    fun getOwnedCardsMap(context: Context, handler: SQLiteHandler): Map<String, List<Boolean>> {
        val output = mutableMapOf<String, List<Boolean>>()

        // Loop through every possible set's lists of cards
        val setList = SetsData.getSetIDs()
        setList.forEach { set ->
            val cardList = getCardList(context, handler, set)
            val ownedCards = mutableListOf<Boolean>()
            cardList.forEach { card ->
                ownedCards.add(card.owned)
            }
            output.put(set, ownedCards)
        }

        return output
    }

    // Reload user data and update card map
    fun reloadUserData(handler: SQLiteHandler, sets: List<String>) {
        sets.forEach{ set ->
            if (cardMap.containsKey(set)) {
                val cardList = cardMap[set]
                if (cardList != null) {
                    val userData = loadUserData(handler, set, cardList)
                    if (userData.isNotEmpty()) {
                        for (i in 0 until userData.count()) {
                            cardMap[set]!![i].owned = userData[i]
                        }
                    }
                }
            }
        }
    }

    private fun getImageUrl(cardID: String): String {
        val set = cardID.substringBeforeLast('-')
        val number = cardID.substringAfterLast('-').toInt().toString()

        return "https://cdn.pockettrade.app/images/webp/es/${set}_${number}_SPA.webp"
    }

    fun changeCardState(handler: SQLiteHandler, set: String, cardIndex: Int) {
        if (cardIndex < 0) return

        val setList = cardMap[set]
        if (setList == null || cardIndex >= setList.count()) return

        val card = setList[cardIndex]
        val isNowOwned = !card.owned
        handler.saveCard(card.id, set, isNowOwned)
        card.owned = isNowOwned
    }
}