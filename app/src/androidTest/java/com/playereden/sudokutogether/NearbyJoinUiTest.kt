package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

internal fun polishActions(join: (Endpoint, String) -> Unit = { _, _ -> },
    cosmetic: (String, String) -> Unit = { _, _ -> }, send: (String) -> Unit = {}): GameActions =
    GameActions(name = {}, host = {}, showJoin = {}, home = {}, join = join, select = {}, number = {},
        toggleNotes = {}, erase = {}, undo = {}, replacePartner = {}, newPuzzle = {}, solo = {}, profile = {},
        hostRace = {}, sendChat = send, chatVisible = {}, cosmetic = cosmetic, uniformNumbers = {},
        touchFeedback = {}, difficulty = {}, hint = {}, dismissHint = {}, autoCleanNotes = {}, daily = {}, openDaily = {})

class NearbyJoinUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun tappingAnOpenNearbyGameJoinsWithoutTypingAnything() {
        var chosen: Endpoint? = null;var code: String? = null
        val room = NearbyRoom("Joseph's puzzle", "192.168.1.20", 41421, "game", requiresCode = false)
        compose.setContent { SudokuTogetherTheme { JoinScreen(GameUiState(rooms = listOf(room)), polishActions(join = { e, c -> chosen = e;code = c })) } }
        compose.onNodeWithText(room.name).performClick()
        compose.runOnIdle { assertEquals(Endpoint(room.host, room.port), chosen);assertEquals("", code) }
        compose.onNodeWithText("Host address").assertDoesNotExist()
    }
    @Test fun aProtectedNearbyGameAsksForOnlyItsCode() {
        var entered: String? = null
        val room = NearbyRoom("Taylor's puzzle", "192.168.1.21", 41422, requiresCode = true)
        compose.setContent { SudokuTogetherTheme { JoinScreen(GameUiState(rooms = listOf(room)), polishActions(join = { _, c -> entered = c })) } }
        compose.onNodeWithText(room.name).performClick()
        compose.onNodeWithText("Enter game code").assertIsDisplayed()
        compose.onNodeWithTag("nearby_code").performTextInput("123")
        compose.onNodeWithTag("nearby_join").assertIsNotEnabled()
        compose.onNodeWithTag("nearby_code").performTextInput("456")
        compose.onNodeWithTag("nearby_join").performClick()
        compose.runOnIdle { assertEquals("123456", entered) }
    }
    @Test fun manualJoiningIsAnExplicitFallbackWithAnOptionalCode() {
        var chosen: Endpoint? = null;var code: String? = null
        compose.setContent { SudokuTogetherTheme { JoinScreen(GameUiState(), polishActions(join = { e, c -> chosen = e;code = c })) } }
        compose.onNodeWithText("Host address").assertDoesNotExist()
        compose.onNodeWithText("Join manually").performClick()
        compose.onNodeWithTag("manual_address").performTextInput("192.168.1.20:41421")
        compose.onNodeWithTag("join_room").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(Endpoint("192.168.1.20", 41421), chosen);assertEquals("", code) }
    }
}
