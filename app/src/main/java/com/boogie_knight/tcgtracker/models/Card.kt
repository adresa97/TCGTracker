package com.boogie_knight.tcgtracker.models

import kotlinx.serialization.Serializable

data class Card(
    val id: String,
    val name: LocalizedName,
    val type: String,
    val origins: List<String>,
    val rarity: String,
    val image: String?,
    var owned: Boolean = false,
    val baby: Boolean = false
)

@Serializable
data class JsonCard(
    val id: String,
    val name: LocalizedName,
    val type: String,
    val origins: List<String>,
    val rarity: String,
    val baby: Boolean = false
)

@Serializable
data class LocalizedName(
    val en: String,
    val es: String
)