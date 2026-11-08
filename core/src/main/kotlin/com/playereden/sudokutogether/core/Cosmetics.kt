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
    val emblem: String = "tile",
    val portrait: String = ""
)

data class Cosmetic(val id: String, val name: String, val requirement: String)

data class CosmeticProgress(val current: Int, val target: Int) {
    val unlocked: Boolean get() = current >= target
}

object Cosmetics {
    private enum class Milestone(val verb: String, val singular: String, val plural: String) {
        FINISH("Complete", "puzzle", "puzzles"),
        SOLO("Solve", "solo puzzle", "solo puzzles"),
        COOP("Complete", "co-op puzzle", "co-op puzzles"),
        RACE("Win", "versus race", "versus races");

        fun describe(target: Int) = if (target == 0) "Starter"
            else "$verb $target ${if (target == 1) singular else plural}"
    }

    private data class CatalogEntry(val id: String, val name: String, val target: Int = 0, val milestone: Milestone = Milestone.SOLO) {
        fun cosmetic() = Cosmetic(id, name, milestone.describe(target))
    }

    private val woodlandEntries = linkedMapOf(
        "owl" to 0, "fox" to 0, "rabbit" to 1, "bear" to 2,
        "badger" to 3, "hedgehog" to 4, "robin" to 5, "deer" to 6,
        "squirrel" to 8, "otter" to 10, "tortoise" to 12, "cat" to 15
    ).map { (animal, count) ->
        CatalogEntry("woodland_$animal", animal.replaceFirstChar { it.uppercase() }, count, Milestone.FINISH)
    }
    private val oceanEntries = listOf(
        CatalogEntry("ocean_octopus", "Octopus", 0, Milestone.FINISH),
        CatalogEntry("ocean_seal", "Harbor Seal", 18, Milestone.FINISH),
        CatalogEntry("ocean_hermit_crab", "Hermit Crab", 25, Milestone.FINISH),
        CatalogEntry("ocean_manta_ray", "Manta Ray", 35, Milestone.FINISH),
        CatalogEntry("ocean_jellyfish", "Jellyfish", 50, Milestone.FINISH),
        CatalogEntry("ocean_narwhal", "Narwhal", 75, Milestone.FINISH)
    )
    private val inkEntries = listOf(
        CatalogEntry("forest", "Forest"),
        CatalogEntry("cobalt", "Cobalt", 1),
        CatalogEntry("amber", "Amber", 3),
        CatalogEntry("rose", "Rose", 5),
        CatalogEntry("violet", "Violet", 2, Milestone.RACE)
    )
    private val boardEntries = listOf(
        CatalogEntry("classic", "Classic"),
        CatalogEntry("linen", "Linen", 2),
        CatalogEntry("midnight", "Midnight", 5),
        CatalogEntry("circuit", "Circuit", 3, Milestone.RACE)
    )
    private val sharedBoardOrder = listOf("classic", "linen", "midnight", "circuit")
    private val fontEntries = listOf(
        CatalogEntry("clear", "Clear"),
        CatalogEntry("serif", "Bookish", 1),
        CatalogEntry("mono", "Terminal", 2, Milestone.COOP)
    )
    private val emblemEntries = listOf(
        CatalogEntry("tile", "Gridkeeper"),
        CatalogEntry("leaf", "First Sprout", 1),
        CatalogEntry("duo", "Two Minds", 1, Milestone.COOP),
        CatalogEntry("flash", "Challenger", 1, Milestone.RACE),
        CatalogEntry("star", "Star Solver", 5),
        CatalogEntry("laurel", "Champion", 3, Milestone.RACE)
    )
    private val entries = (woodlandEntries + oceanEntries + inkEntries + boardEntries + fontEntries + emblemEntries).associateBy { it.id }

    val woodlandPortraits = woodlandEntries.map(CatalogEntry::cosmetic)
    val oceanPortraits = oceanEntries.map(CatalogEntry::cosmetic)
    val portraits = woodlandPortraits + oceanPortraits
    val inks = inkEntries.map(CatalogEntry::cosmetic)
    val boards = boardEntries.map(CatalogEntry::cosmetic)
    val fonts = fontEntries.map(CatalogEntry::cosmetic)
    val emblems = emblemEntries.map(CatalogEntry::cosmetic)

    /** The room's display priority is independent of either player's unlocks. */
    fun sharedCoopBoard(hostBoard: String, guestBoard: String?): String =
        sharedBoardOrder[maxOf(0, sharedBoardOrder.indexOf(hostBoard), sharedBoardOrder.indexOf(guestBoard))]

    fun completedPuzzles(records: List<GameRecord>): Int = records.distinctBy { it.id }.count {
        when (it.mode) {
            SOLO, DAILY, COOP -> it.result == "SOLVED"
            VERSUS -> it.result == "WIN"
            else -> false
        }
    }

    /** The same milestone controls the preview and the equip permission. */
    fun progress(id: String, records: List<GameRecord>): CosmeticProgress? {
        val entry = entries[id] ?: return null
        if (entry.target == 0) return CosmeticProgress(0, 0)
        val current = when (entry.milestone) {
            Milestone.FINISH -> completedPuzzles(records)
            Milestone.SOLO -> records.asSequence().distinctBy { it.id }.count { it.mode == SOLO && it.result == "SOLVED" }
            Milestone.COOP -> records.asSequence().distinctBy { it.id }.count { it.mode == COOP && it.result == "SOLVED" }
            Milestone.RACE -> records.asSequence().distinctBy { it.id }.count { it.mode == VERSUS && it.result == "WIN" }
        }
        return CosmeticProgress(current, entry.target)
    }

    fun valid(a: Appearance): Boolean = inks.any { it.id == a.ink } &&
        boards.any { it.id == a.board } && fonts.any { it.id == a.font } &&
        emblems.any { it.id == a.emblem } && (a.portrait.isEmpty() || portraits.any { it.id == a.portrait })
    fun allowed(id: String, records: List<GameRecord>): Boolean = progress(id, records)?.unlocked == true
    fun accessible(a: Appearance, records: List<GameRecord>): Appearance = Appearance(
        ink = a.ink.takeIf { allowed(it, records) } ?: "forest",
        board = a.board.takeIf { allowed(it, records) } ?: "classic",
        font = a.font.takeIf { allowed(it, records) } ?: "clear",
        emblem = a.emblem.takeIf { allowed(it, records) } ?: "tile",
        portrait = a.portrait.takeIf { it.isEmpty() || (portraits.any { item -> item.id == it } && allowed(it, records)) } ?: ""
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
