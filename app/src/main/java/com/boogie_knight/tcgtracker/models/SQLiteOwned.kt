package com.boogie_knight.tcgtracker.models

data class SQLOwnedCard(
    val id: String,
    val game: String,
    val set: String,
    val isOwned: Boolean
)

data class SQLOwnedSet(
    val set: String,
    val game: String,
    val booster: String = "",
    val rarity: String = "",
    val owned: Int,
    val total: Int
)

data class SQLFilterConfig(
    val filter: String,
    val game: String,
    val type: String,
    val state: Int
)