package com.playereden.sudokutogether

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.GUEST
import com.playereden.sudokutogether.core.HOST
import com.playereden.sudokutogether.core.RoomStatus
import com.playereden.sudokutogether.core.VERSUS
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VersusPresenceUiTest {
    @get:Rule val compose = createComposeRule()

    private val base = polishState()
    private val coOp = requireNotNull(base.snapshot)
    private val given = coOp.puzzle.clues.count { it != 0 }
    private val target = coOp.puzzle.clues.size - given
    private val race = coOp.copy(
        mode = VERSUS, hostProgress = given + 3, guestProgress = given + 7,
        hostAppearance = Appearance(portrait = "woodland_owl"),
        guestAppearance = Appearance(portrait = "woodland_fox")
    )

    @Test fun raceUsesTheOpenSeatThenThePlayersActualNamesAndCounts() {
        val state = mutableStateOf(base.copy(
            snapshot = race.copy(guestName = null, hostProgress = given, guestProgress = given),
            connected = false, status = RoomStatus()
        ))
        compose.setContent { SudokuTogetherTheme { PlayScreen(state.value, polishActions(), {}, {}, {}, {}) } }

        compose.onNodeWithText("Waiting for your rival").assertIsDisplayed()
        compose.onNodeWithText("Open seat").assertIsDisplayed()
        compose.onAllNodesWithText("0 of $target filled").assertCountEquals(1)

        compose.runOnIdle {
            state.value = base.copy(snapshot = race, status = RoomStatus(guestActive = true, reserved = true))
        }
        compose.onNodeWithTag("versus_presence").assertIsDisplayed()
        laneText("versus_you", "Joseph").assertIsDisplayed()
        laneText("versus_you", "3 of $target filled").assertIsDisplayed()
        laneText("versus_rival", "Taylor").assertIsDisplayed()
        laneText("versus_rival", "7 of $target filled").assertIsDisplayed()

        compose.runOnIdle {
            state.value = state.value.copy(snapshot = race.copy(epoch = "next", hostProgress = given, guestProgress = given))
        }
        laneText("versus_you", "0 of $target filled").assertIsDisplayed()
        laneText("versus_rival", "0 of $target filled").assertIsDisplayed()
    }

    @Test fun aDisconnectedGuestCanReconnectEvenAfterTheRaceFinishes() {
        var resumed = false
        val state = mutableStateOf(base.copy(role = GUEST, connected = false, snapshot = race))
        val actions = polishActions().copy(showJoin = { resumed = it })
        compose.setContent { SudokuTogetherTheme { PlayScreen(state.value, actions, {}, {}, {}, {}) } }

        compose.onNodeWithText("Connection lost").assertIsDisplayed()
        laneText("versus_you", "Taylor").assertIsDisplayed()
        laneText("versus_rival", "Joseph").assertIsDisplayed()
        compose.onNodeWithText("Reconnect").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertTrue(resumed)
            state.value = state.value.copy(snapshot = race.copy(completed = true, winner = HOST))
        }
        compose.onNodeWithText("Your rival won").assertIsDisplayed()
        compose.onNodeWithText("Race complete").assertExists()
        compose.onNodeWithText("Reconnect").performScrollTo().assertIsDisplayed()

        compose.runOnIdle { state.value = state.value.copy(connected = true) }
        compose.onNodeWithText("Reconnect").assertDoesNotExist()
    }

    @Test fun largeTextOnAShortPhoneKeepsTheBoardAndKeypadReachable() {
        val hostName = "Christopher Longlastname"
        val guestName = "Alexandria Longlastname"
        val state = base.copy(snapshot = race.copy(
            hostName = hostName, guestName = guestName
        ))
        compose.setContent {
            SudokuTogetherTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    Box(Modifier.width(320.dp).height(480.dp)) {
                        PlayScreen(state, polishActions(), {}, {}, {}, {})
                    }
                }
            }
        }

        compose.onNodeWithText("Versus").assertIsDisplayed()
        compose.onNodeWithTag("versus_presence").performScrollTo().assertIsDisplayed()
        for ((lane, name) in listOf("versus_you" to hostName, "versus_rival" to guestName)) {
            val layout = mutableListOf<TextLayoutResult>()
            laneText(lane, name).assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            assertTrue("$name should wrap at large text", layout.single().lineCount > 1)
        }
        compose.onNodeWithTag("cell_40").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("number_9").performScrollTo().assertIsDisplayed()
    }

    private fun laneText(tag: String, text: String): SemanticsNodeInteraction =
        compose.onNode(hasText(text) and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)
}
