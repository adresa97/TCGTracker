package com.example.tcgtracker.models

data class Card(
    val id: String,
    val name: CardName,
    val type: String,
    val boosters: List<Booster>,
    val rarity: Rarity,
    val image: String?,
    var owned: Boolean = false,
    val baby: Boolean = false
)

data class InnerJsonCard(
    val id: String,
    val name: CardName,
    val type: String,
    val boosters: List<String>,
    val rarity: String,
    val baby: Boolean = false
)

data class CardName(
    val en: String,
    val es: String
)

enum class Booster(val prettyName: String) {
    // Values
    ERROR(""),
    MEWTWO("Mewtwo"),
    CHARIZARD("Charizard"),
    PIKACHU("Pikachu"),
    MEW("Mew"),
    DIALGA("Dialga"),
    PALKIA("Palkia"),
    ARCEUS("Arceus"),
    SHINY_CHARIZARD("Shiny Charizard"),
    SOLGALEO("Solgaleo"),
    LUNALA("Lunala"),
    BUZZWOLE("Buzzwole"),
    EEVEE("Eevee"),
    LUGIA("Lugia"),
    HO_OH("Ho-Oh");

    // Custom functions
    companion object {
        private val map = Booster.entries.associateBy { it.prettyName }
        infix fun fromPrettyName(value: String): Booster {
            return map[value] ?: Booster.ERROR
        }
    }
}

enum class Rarity(val prettyName: String, val symbol: String) {
    // Values
    ERROR("", ""),
    ONE_DIAMOND("One Diamond", "♢"),
    TWO_DIAMOND("Two Diamond", "♢♢"),
    THREE_DIAMOND("Three Diamond", "♢♢♢"),
    FOUR_DIAMOND("Four Diamond", "♢♢♢♢"),
    ONE_STAR("One Star", "☆"),
    TWO_STAR("Two Star", "☆☆"),
    THREE_STAR("Three Star", "☆☆☆"),
    ONE_SHINY("One Shiny", "⛭"),
    TWO_SHINY("Two Shiny", "⛭⛭"),
    CROWN("Crown", "♛");

    // Custom functions
    companion object {
        private val map = Rarity.entries.associateBy { it.prettyName }
        infix fun fromPrettyName(value: String): Rarity {
            return map[value] ?: Rarity.ERROR
        }
    }
}