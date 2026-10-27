package com.playereden.sudokutogether.core

import kotlinx.serialization.Serializable

/**
 * Only curated identifiers travel over the network; never send arbitrary fonts,
 * downloadable assets, colors or executable/custom style data.
 */
@Serializable data class Appearance(
    val ink: String = "forest",
    val board: String = "classic",
    val font: String = "clear",
    val emblem: String = "tile"
)

data class Cosmetic(val id: String, val name: String, val requirement: String)

object Cosmetics {
    val inks = listOf(
        Cosmetic("forest", "Forest", "Starter"),
        Cosmetic("cobalt", "Cobalt", "Solve 1 solo puzzle"),
        Cosmetic("amber", "Amber", "Solve 3 solo puzzles"),
        Cosmetic("rose", "Rose", "Solve 5 solo puzzles"),
        Cosmetic("violet", "Violet", "Win 2 versus races")
    )
    val boards = listOf(
        Cosmetic("classic", "Classic", "Starter"),
        Cosmetic("linen", "Linen", "Solve 2 solo puzzles"),
        Cosmetic("midnight", "Midnight", "Solve 5 solo puzzles"),
        Cosmetic("circuit", "Circuit", "Win 3 versus races")
    )
    val fonts = listOf(
        Cosmetic("clear", "Clear", "Starter"),
        Cosmetic("serif", "Bookish", "Solve 1 solo puzzle"),
        Cosmetic("mono", "Terminal", "Complete 2 co-op puzzles")
    )
    val emblems = listOf(
        Cosmetic("tile", "Gridkeeper", "Starter"),
        Cosmetic("leaf", "First Sprout", "Solve 1 solo puzzle"),
        Cosmetic("duo", "Two Minds", "Complete 1 co-op puzzle"),
        Cosmetic("flash", "Challenger", "Win 1 versus race"),
        Cosmetic("star", "Star Solver", "Solve 5 solo puzzles"),
        Cosmetic("laurel", "Champion", "Win 3 versus races")
    )
    fun valid(a: Appearance): Boolean = inks.any { it.id == a.ink } &&
        boards.any { it.id == a.board } && fonts.any { it.id == a.font } &&
        emblems.any { it.id == a.emblem }
    fun allowed(id: String, records: List<GameRecord>): Boolean {
        val unique = records.distinctBy { it.id }
        val solo = unique.count { it.mode == SOLO && it.result == "SOLVED" }
        val wins = unique.count { it.mode == VERSUS && it.result == "WIN" }
        val coop = unique.count { it.mode == COOP && it.result == "SOLVED" }
        return when (id) {
            "forest", "classic", "clear", "tile" -> true
            "cobalt", "serif", "leaf" -> solo >= 1
            "duo" -> coop >= 1
            "flash" -> wins >= 1
            "star" -> solo >= 5
            "laurel" -> wins >= 3
            "linen" -> solo >= 2
            "amber" -> solo >= 3
            "rose", "midnight" -> solo >= 5
            "violet" -> wins >= 2
            "circuit" -> wins >= 3
            "mono" -> coop >= 2
            else -> false
        }
    }
    fun accessible(a: Appearance, records: List<GameRecord>): Appearance = Appearance(
        ink = a.ink.takeIf { allowed(it, records) } ?: "forest",
        board = a.board.takeIf { allowed(it, records) } ?: "classic",
        font = a.font.takeIf { allowed(it, records) } ?: "clear",
        emblem = a.emblem.takeIf { allowed(it, records) } ?: "tile"
    )
}

@Serializable data class ChatLine(
    val id: String, val player: String, val text: String, val atMs: Long
)

object ChatRules {
    const val MAX_LENGTH = 160
    const val HISTORY_LIMIT = 30
    const val MIN_INTERVAL_MS = 650L
    fun clean(raw: String): String = raw.filter { it == ' ' || !it.isISOControl() }
        .trim().take(MAX_LENGTH)
}
