package com.playereden.sudokutogether.core

/**
 * Achievements are derived from durable, locally stored match records rather than
 * cached counters. This avoids double-unlocks after reconnect or repeated reload.
 * These are personal/offline milestones, not server-verified ranked rewards.
 */
data class AchievementProgress(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int
) {
    val unlocked: Boolean get() = current >= target
}

object Achievements {
    fun progress(records: List<GameRecord>): List<AchievementProgress> {
        val completed = records.distinctBy { it.id }
        val solo = completed.filter { it.mode == SOLO && it.result == "SOLVED" }
        val coop = completed.count { it.mode == COOP && it.result == "SOLVED" }
        val wins = completed.count { it.mode == VERSUS && it.result == "WIN" }
        val races = completed.count { it.mode == VERSUS && it.result in listOf("WIN", "LOSS", "DRAW") }
        val fastest = solo.minOfOrNull { it.elapsedMs }
        val speed = if (fastest != null && fastest > 0 && fastest <= 10 * 60_000L) 1 else 0
        return listOf(
            AchievementProgress("first-grid", "First Grid", "Complete your first solo Sudoku", solo.size, 1),
            AchievementProgress("steady-hand", "Steady Hand", "Complete five solo puzzles", solo.size, 5),
            AchievementProgress("dedicated", "Dedicated Solver", "Complete twenty solo puzzles", solo.size, 20),
            AchievementProgress("two-minds", "Two Minds", "Finish a co-op board together", coop, 1),
            AchievementProgress("teamwork", "Teamwork", "Finish five co-op boards", coop, 5),
            AchievementProgress("challenger", "Challenger", "Win your first versus race", wins, 1),
            AchievementProgress("rivalry", "Friendly Rivalry", "Play five versus races", races, 5),
            AchievementProgress("champion", "Champion", "Win three versus races", wins, 3),
            AchievementProgress("quick-thinker", "Quick Thinker", "Solve a solo puzzle within ten minutes (assistance allowed)", speed, 1)
        )
    }

    fun earnedCount(records: List<GameRecord>): Int = progress(records).count { it.unlocked }
}
