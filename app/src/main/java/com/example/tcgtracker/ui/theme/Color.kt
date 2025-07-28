package com.example.tcgtracker.ui.theme

import androidx.compose.ui.graphics.Color

val PocketWhite = Color(0xFFEEF6FB)
val PocketBlack = Color(0xFF23272E)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

data class ContainerContentColors(
    val container: Color,
    val content: Color
)

// Set color
val setColors = mapOf<String, Color>(
    Pair("P", Color(0xFF2f6eff)),
    Pair("A1", Color(0xFF9445f4)),
    Pair("A1a", Color(0xFF129e6f)),
    Pair("A2", Color(0xFF7a8594)),
    Pair("A2a", Color(0xFFc17c17)),
    Pair("A2b", Color(0xFF5495b7)),
    Pair("A3", Color(0xFF2c67ec)),
    Pair("A3a", Color(0xFFe21616)),
    Pair("A3b", Color(0xFFB3591E)),
    Pair("A4", Color(0xFF9a8152))
)

// Booster color
val boosterColors = mapOf<String, Color>(
    Pair("All", Color(0xFFefefef)),
    Pair("Unlockable", Color(0xFFCCCCCC)),
    Pair("Mewtwo", Color(0xFFd9d2e9)),
    Pair("Charizard", Color(0xFFf4cccc)),
    Pair("Pikachu", Color(0xFFfff2cc)),
    Pair("Mew", Color(0xFFffe1f5)),
    Pair("Dialga", Color(0xFFc9daf8)),
    Pair("Palkia", Color(0xFFffcce0)),
    Pair("Arceus", Color(0xFFf7ebb3)),
    Pair("Shiny Charizard", Color(0xFFd4a1af)),
    Pair("Solgaleo", Color(0xFFffe7b0)),
    Pair("Lunala", Color(0xFFb9b7e0)),
    Pair("Buzzwole", Color(0xFFff9d7f)),
    Pair("Eevee", Color(0xFFf9bd93)),
    Pair("Ho-oh", Color(0xFFedd27f)),
    Pair("Lugia", Color(0xFFa7c0c6)),
)