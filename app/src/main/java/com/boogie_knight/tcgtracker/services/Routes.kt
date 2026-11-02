package com.boogie_knight.tcgtracker.services

object Routes {
    enum class Game(val route: String) {
        POCKET("PTCGPocket/"),
        ALYSION("DAlysion/")
    }

    enum class File(val route: String) {
        CONCEPTS("concepts.json"),
        ORIGINS("origins.json"),
        SETS("sets.json"),
        IMAGES("imageSources.json"),
        CARDS("cards/")
    }

    fun getRoute(game: Game, file: File): String {
        return game.route + file.route
    }

    fun getRoute(gameRoute: String, file: File): String {
        return gameRoute + file.route
    }
}