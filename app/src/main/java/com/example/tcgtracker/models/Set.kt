package com.example.tcgtracker.models

import androidx.compose.ui.graphics.Color
import com.example.tcgtracker.R

data class InnerJsonSet(
    val series: String,
    val expansion: String,
    val set: String,
    val name: String,
    val color: List<Float>,
    val cardCount: Int,
    val origins: List<String>
)

data class ExternalJsonSet (
    val values: List<Boolean>
)

data class Set(
    val series: String,
    val expansion: String,
    val set: String,
    val name: String,
    val cover: CoverImage,
    val color: Color,
    val cardCount: Int,
    val origins: List<String>,
    var numbers: OwnedSetData
)

data class OwnedSetData(
    val all: OwnedData,
    val byBooster: Map<String, Map<String, OwnedData>>,
    val byRarity: Map<String, OwnedData>
)

data class OwnedData(
    val totalCards: Int,
    val ownedCards: Int
)

enum class CoverImage(val id: Int) {
    // Values
    EMPTY(R.drawable.a_promo_en),
    P_A(R.drawable.a_promo_en),
    A1(R.drawable.a1_genetic_apex_en),
    A1a(R.drawable.a1a_mythical_island_en),
    A2(R.drawable.a2_spacetime_smackdown_en),
    A2a(R.drawable.a2a_triumphant_light_en),
    A2b(R.drawable.a2b_shining_revelry_en),
    A3(R.drawable.a3_celestial_guardians_en),
    A3a(R.drawable.a3a_extradimensional_crisis_en),
    A3b(R.drawable.a3b_eevee_grove_en),
    A4(R.drawable.a4_wisdomofseaandsky_en);

    // Custom functions
    companion object {
        private val map = CoverImage.entries.associateBy { it.name }
        infix fun from(value: String): CoverImage {
            val fixedName = value.replace('-', '_')
            return map[fixedName] ?: CoverImage.EMPTY
        }
    }
}