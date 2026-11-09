package com.boogie_knight.tcgtracker.models

import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImagePainter
import kotlinx.serialization.Serializable

data class Game(
    val name: String,
    val compactName: String,
    val folder: String,
    val logo: String,
    var cardBack: Pair<String, AsyncImagePainter?>,
    val color: Color,
    val concepts: String,
    val origins: String,
    val sets: String,
    val imageSources: String,
    val cards: String
)

data class GameIcon(
    val name: String,
    val logo: String,
    val color: Color
)

@Serializable
data class JsonGame(
    val key: String,
    val name: String,
    val compactName: String,
    val folder: String,
    val logo: String,
    val cardBack: String = "",
    val color: List<Float>,
    val concepts: String,
    val origins: String,
    val sets: String,
    val imageSources: String,
    val cards: String
)