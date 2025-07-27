package com.example.tcgtracker.models

data class Card(
    val id: String,
    val name: String,
    val type: String,
    val booster: List<String>,
    val rarity: String,
    val image: String?
)

val rarityMap = mapOf(
    Pair("One Diamond", "♢"),
    Pair("Two Diamond", "♢♢"),
    Pair("Three Diamond", "♢♢♢"),
    Pair("Four Diamond", "♢♢♢♢"),
    Pair("One Star", "☆"),
    Pair("Two Star", "☆☆"),
    Pair("Three Star", "☆☆☆"),
    Pair("One Shiny", "⛭"),
    Pair("Two Shiny", "⛭⛭"),
    Pair("Crown", "♛")
)