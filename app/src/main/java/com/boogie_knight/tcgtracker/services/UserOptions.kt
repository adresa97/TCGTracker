package com.boogie_knight.tcgtracker.services

object UserOptions {
    enum class StartScreen(val screenName: String) {
        SELECTOR("Principal"),
        LAST("Última visitada")
    }

    private val startScreens = mutableMapOf<String, String>()

    fun initUserOptions() {
        StartScreen.entries.forEach{ entry->
            startScreens.put(entry.name, entry.screenName)
        }

        val games = GamesData.getGamesList()
        games.forEach{ game->
            startScreens.put(game, GamesData.getGameCompactName(game))
        }
    }

    fun getStartScreenMap(): Map<String, String> { return startScreens }

    fun getStartScreenName(code: String): String {
        return startScreens[code] ?: ""
    }
}