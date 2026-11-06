package com.boogie_knight.tcgtracker.services

import androidx.compose.ui.graphics.Color
import com.boogie_knight.tcgtracker.models.Game
import com.boogie_knight.tcgtracker.models.GameIcon
import com.boogie_knight.tcgtracker.models.JsonGame
import com.boogie_knight.tcgtracker.repositories.AssetsRepository
import com.boogie_knight.tcgtracker.utils.ParseJSON

object GamesData {
    const val ASSETS_GAMES_JSON_ROUTE = "games.json"
    private val games = mutableMapOf<String, Game>()

    // Load json data
    fun loadGamesData() {
        val jsonString = AssetsRepository.getData(ASSETS_GAMES_JSON_ROUTE)
        val jsonData = ParseJSON(jsonString, Array<JsonGame>::class.java)?.asList()

        if (jsonData == null) return
        jsonData.forEach{ game ->
            val color = Color(game.color[0] / 255, game.color[1] / 255, game.color[2] / 255)
            games.put(game.key, Game(
                name = game.name,
                folder = game.folder,
                logo = game.logo,
                color = color,
                concepts = game.concepts,
                origins = game.origins,
                sets = game.sets,
                imageSources = game.imageSources,
                cards = game.cards
            ))
        }
    }

    // Get games list
    fun getGamesList(): List<String> { return games.keys.toList() }

    // Get games map
    fun getGameIcons(): Map<String, GameIcon> {
        val gameIcons = mutableMapOf<String, GameIcon>()
        games.forEach{ game ->
            gameIcons.put(key = game.key, value = GameIcon(
                name = game.value.name,
                logo = game.value.logo,
                color = game.value.color
            ))
        }
        return gameIcons
    }

    // Get game name
    fun getGameName(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.name
    }

    // Getters for each individual path
    fun getGameFolderRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.folder
    }

    fun getGameLogoRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.logo
    }

    fun getGameConceptsRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.concepts
    }

    fun getGameOriginsRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.origins
    }

    fun getGameSetsRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.sets
    }

    fun getGameImageSourcesRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.imageSources
    }

    fun getGameCardsFolderRoute(key: String): String {
        val game = games[key]
        if (game == null) return ""
        return game.cards
    }

    fun isGameInData(game: String): Boolean {
        return games[game] != null
    }
}