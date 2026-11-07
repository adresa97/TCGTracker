package com.boogie_knight.tcgtracker.models

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class InnerJsonSet(
    val series: String,
    val expansion: String,
    val set: String,
    val name: LocalizedName,
    val cover: String,
    val color: List<Float>,
    val cardCount: Int,
    val origins: List<String>
)

@Serializable
data class ExternalJsonSet (
    val values: List<Boolean>
)

data class Set(
    val series: String,
    val expansion: String,
    val set: String,
    val name: LocalizedName,
    val cover: String,
    val color: Color,
    val cardCount: Int,
    val origins: List<String>,
    var numbers: OwnedSetData
)

data class OwnedSetData(
    val all: OwnedData,
    val byBooster: Map<String, OwnedBoosterData>,
    val byRarity: Map<String, OwnedData>?
)

data class OwnedBoosterData(
    val all: OwnedData,
    val byRarity: Map<String, OwnedData>?
)

data class OwnedData(
    val totalCards: Int,
    val ownedCards: Int
)