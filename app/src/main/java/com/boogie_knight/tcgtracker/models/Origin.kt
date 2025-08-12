package com.boogie_knight.tcgtracker.models

import androidx.compose.ui.graphics.Color

data class Origin (
    val id: String,
    val name: LocalizedName,
    val type: String,
    val color: Color,
    val cardCount: Int,
    val odds: Map<String, List<Float>>?
)

data class JsonOrigin (
    val id: String,
    val name: LocalizedName,
    val type: String,
    val color: List<Float>,
    val cardCount: Int,
    val odds: Map<String, List<Float>>?
)

data class JsonConcepts (
    val types: Map<String, String>,
    val rarities: Map<String, String>
)