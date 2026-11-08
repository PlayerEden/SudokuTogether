package com.playereden.sudokutogether

import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.GUEST
import com.playereden.sudokutogether.core.HOST
import com.playereden.sudokutogether.core.RoomStatus
import com.playereden.sudokutogether.core.Snapshot

internal enum class VersusPhase(val label: String) {
    WAITING("Waiting for your rival"),
    LIVE("Race in progress"),
    PAUSED("Race paused"),
    DISCONNECTED("Connection lost"),
    FINISHED("Race complete")
}

internal data class VersusPlayer(val name: String, val appearance: Appearance, val filled: Int)

internal data class VersusPresenceState(
    val self: VersusPlayer,
    val rival: VersusPlayer?,
    val target: Int,
    val phase: VersusPhase
)

internal fun versusPresenceState(
    snapshot: Snapshot,
    role: String,
    status: RoomStatus,
    connected: Boolean
): VersusPresenceState {
    val given = snapshot.puzzle.clues.count { it != 0 }
    val target = snapshot.puzzle.clues.size - given
    fun filled(progress: Int) = (progress - given).coerceIn(0, target)

    val host = VersusPlayer(snapshot.hostName, snapshot.hostAppearance, filled(snapshot.hostProgress))
    val guest = VersusPlayer(snapshot.guestName ?: "Guest", snapshot.guestAppearance, filled(snapshot.guestProgress))
    val phase = when {
        snapshot.completed -> VersusPhase.FINISHED
        role == GUEST && !connected -> VersusPhase.DISCONNECTED
        snapshot.guestName == null -> VersusPhase.WAITING
        !status.hostActive || !status.guestActive -> VersusPhase.PAUSED
        else -> VersusPhase.LIVE
    }
    return if (role == HOST) VersusPresenceState(host, guest.takeIf { snapshot.guestName != null }, target, phase)
    else VersusPresenceState(guest, host, target, phase)
}
