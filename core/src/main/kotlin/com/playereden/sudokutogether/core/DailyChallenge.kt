package com.playereden.sudokutogether.core

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId

/** Offline, local-calendar daily challenge. Stable for a given YYYY-MM-DD on every device. */
object DailyChallenge {
    const val MAX_SAVED_DAYS = 31
    const val DIFFICULTY = "EASY"
    private const val SALT = "SudokuTogether/daily-v1/"

    fun today(zone: ZoneId = ZoneId.systemDefault(), instant: java.time.Instant = java.time.Instant.now()): String =
        LocalDate.ofInstant(instant, zone).toString()

    fun puzzle(date: String): Puzzle {
        // Exact ISO parsing makes date identity explicit, unambiguous and testable.
        require(LocalDate.parse(date).toString() == date) { "Date must be ISO yyyy-MM-dd" }
        var attempt = 0
        val rated = PuzzleSupply.nextRated(HumanSudoku.Grade.EASY, emptyList(), {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest("$SALT$date/${attempt++}".toByteArray(Charsets.UTF_8))
            ByteBuffer.wrap(bytes, 0, 8).long
        })
        return rated.puzzle
    }

    fun open(data: LocalPlayerData, date: String, nowMs: Long): LocalPlayerData {
        if (data.dailyProgress.any { it.date == date }) return data
        val progress = DailyProgress(date, PersonalPlay.newSolo(puzzle(date), nowMs))
        // Keep every unfinished day: a rolling history limit must never silently
        // discard a player's in-progress puzzle. Trim only finished entries.
        val all = data.dailyProgress + progress
        val unfinished = all.filterNot { it.solo.completed }
        val finished = all.filter { it.solo.completed }.takeLast(
            (MAX_SAVED_DAYS - unfinished.size).coerceAtLeast(0)
        )
        return data.copy(dailyProgress = (unfinished + finished).sortedBy { it.date })
    }

    fun find(data: LocalPlayerData, date: String): SoloProgress? =
        data.dailyProgress.firstOrNull { it.date == date }?.solo

    fun update(data: LocalPlayerData, date: String, progress: SoloProgress): LocalPlayerData {
        require(data.dailyProgress.any { it.date == date && it.solo.id == progress.id })
        return data.copy(dailyProgress = data.dailyProgress.map {
            if (it.date == date) it.copy(solo = progress) else it
        })
    }

    fun complete(data: LocalPlayerData, date: String, progress: SoloProgress): LocalPlayerData {
        require(progress.completed && Sudoku.complete(progress.puzzle, progress.cells.map { it.value }))
        val updated = update(data, date, progress)
        if (updated.records.any { it.id == progress.id }) return updated
        val record = GameRecord(progress.id, DAILY, "SOLVED", progress.puzzle.id,
            progress.elapsedMs, progress.moves, progress.finishedAtMs!!,
            progress.puzzle.difficulty, assisted = progress.hintsUsed > 0 || progress.usedNoteAssist,
            hintsUsed = progress.hintsUsed, dailyDate = date)
        return updated.copy(records = (updated.records + record).takeLast(MAX_RECORDS))
    }

    fun completedDays(records: List<GameRecord>): Int =
        records.filter { it.mode == DAILY && it.result == "SOLVED" && it.dailyDate != null }
            .map { it.dailyDate }.distinct().size
}
