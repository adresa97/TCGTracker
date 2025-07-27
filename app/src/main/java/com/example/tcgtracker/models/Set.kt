package com.example.tcgtracker.models

import com.example.tcgtracker.R

data class Set(
    val series: String,
    val expansion: String,
    val set: String,
    val name: String,
    var cover: Int
)

val coverMap = mapOf<String, Int>(
    Pair("P-A", R.drawable.a_promo_en),
    Pair("A1", R.drawable.a1_genetic_apex_en),
    Pair("A1a", R.drawable.a1a_mythical_island_en),
    Pair("A2", R.drawable.a2_spacetime_smackdown_en),
    Pair("A2a", R.drawable.a2a_triumphant_light_en),
    Pair("A2b", R.drawable.a2b_shining_revelry_en),
    Pair("A3", R.drawable.a3_celestial_guardians_en),
    Pair("A3a", R.drawable.a3a_extradimensional_crisis_en),
    Pair("A3b", R.drawable.a3b_eevee_grove_en)
)

val emptyCover = R.drawable.a_promo_en