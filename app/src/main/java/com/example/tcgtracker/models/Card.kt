package com.example.tcgtracker.models

data class Card(
    val id: String,
    val name: CardName,
    val type: String,
    val origins: List<String>,
    val rarity: String,
    val image: String?,
    var owned: Boolean = false,
    val baby: Boolean = false
)

data class InnerJsonCard(
    val id: String,
    val name: CardName,
    val type: String,
    val origins: List<String>,
    val rarity: String,
    val baby: Boolean = false
)

data class CardName(
    val en: String,
    val es: String
)