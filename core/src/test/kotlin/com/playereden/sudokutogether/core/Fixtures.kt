package com.playereden.sudokutogether.core

val solution = "123456789456789123789123456234567891567891234891234567345678912678912345912345678".map { it.digitToInt() }
fun fixture(): Puzzle = Puzzle("fixture", solution.mapIndexed { i, v -> if (i in listOf(0, 1, 9, 10)) 0 else v })
fun Snapshot.move(cell: Int, kind: Kind = Kind.VALUE, number: Int = 1, enabled: Boolean = true): Operation =
    Operation(sessionId, epoch, cell, cells[cell].revision, kind, number, enabled)
fun session(): GameSession = GameSession.create(fixture()).also { it.admitGuest("Partner"); it.setPresence() }
