package com.example.tcgtracker.models

data class SQLOwnedCard(
    val id: String,
    val set: String,
    val isOwned: Boolean
)

data class SQLOwnedSet(
    val set: String,
    val booster: String = "",
    val rarity: String = "",
    val owned: Int,
    val total: Int
)