package com.boogie_knight.tcgtracker.models

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

data class Origin (
    val id: String,
    val name: LocalizedName,
    val type: String,
    val color: Color,
    val cardCount: Int,
    val packSize: Int,
    val odds: Map<String, List<Float>>?
)

@Serializable
data class JsonOrigin (
    val id: String,
    val name: LocalizedName,
    val type: String,
    val color: List<Float>,
    val cardCount: Int,
    val packSize: Int?,
    val odds: Map<String, List<Float>>?
)

@Serializable
data class JsonConcepts (
    val types: Map<String, String>,
    val rarities: Map<String, String>,
    val parallel: Map<String, String>?
)