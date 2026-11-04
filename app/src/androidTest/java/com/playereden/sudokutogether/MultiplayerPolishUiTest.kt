package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

internal fun polishState(unread: Int = 0): GameUiState {
    val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    val puzzle = Puzzle("polish", clues, "EASY")
    return GameUiState(page = Page.PLAY, role = HOST, name = "Joseph", roomCode = "123456", address = "192.168.1.20:41421",
        connected = true, unreadChat = unread, status = RoomStatus(guestActive = true),
        snapshot = Snapshot("polish", "epoch", puzzle, clues.map { Cell(it) }, hostName = "Joseph", guestName = "Taylor"))
}
class MultiplayerPolishUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun unreadCountDoesNotSplitTheRoomTitleAtNormalPhoneWidth() {
        compose.setContent { SudokuTogetherTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.15f)) {
            Box(Modifier.width(360.dp).fillMaxHeight()) { PlayScreen(polishState(999), polishActions(), {}, {}, {}, {}) }
        } } }
        val result = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Co-op").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(result) }
        assertEquals("The title must not wrap when unread chat grows", 1, result.single().lineCount)
        compose.onNodeWithText("Game details").assertIsDisplayed()
        compose.onNodeWithText("Copy code").assertDoesNotExist()
    }
    @Test fun largeTextKeepsHeaderActionsAndRoomCodeReachable() {
        compose.setContent { SudokuTogetherTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
            Box(Modifier.width(320.dp).fillMaxHeight()) { PlayScreen(polishState(1), polishActions(), {}, {}, {}, {}) }
        } } }
        compose.onNodeWithText("Co-op").assertIsDisplayed()
        compose.onNodeWithText("Game details").performClick()
        compose.onNodeWithText("Copy code").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Room code 123456").assertExists()
    }
    @Test fun chatUsesYouAndEveryQuickReplyFitsWithoutHorizontalHunting() {
        val sent = mutableListOf<String>()
        val state = polishState().copy(chat = listOf(ChatLine("a", HOST, "Hello", 1), ChatLine("b", GUEST, "Hi", 2)))
        compose.setContent { SudokuTogetherTheme { RoomChatSheet(state, polishActions(send = sent::add), {}) } }
        compose.onNodeWithText("You").assertIsDisplayed();compose.onNodeWithText("Taylor").assertIsDisplayed()
        for (reply in listOf("Nice move!", "Need a hint?", "Good game!", "Rematch?")) compose.onNodeWithText(reply).performClick()
        compose.runOnIdle { assertEquals(4, sent.size) }
    }
    @Test fun largeTextChatKeepsTheComposerAndSendActionUsable() {
        var sent: String? = null
        compose.setContent { SudokuTogetherTheme { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
            RoomChatSheet(polishState(), polishActions(send = { sent = it }), {})
        } } }
        compose.onNodeWithTag("chat_input").performClick().performTextInput("Hello Taylor")
        compose.onNodeWithText("Send").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("Hello Taylor", sent) }
    }
}
