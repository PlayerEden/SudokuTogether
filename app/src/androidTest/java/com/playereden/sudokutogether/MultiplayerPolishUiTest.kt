package com.playereden.sudokutogether

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playereden.sudokutogether.core.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

internal fun polishState(unread: Int = 0): GameUiState {
    val clues = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    val puzzle = Puzzle("polish", clues, "EASY")
    return GameUiState(page = Page.PLAY, role = HOST, name = "Joseph", roomCode = "123456", address = "192.168.1.20:41421",
        connected = true, unreadChat = unread, status = RoomStatus(guestActive = true),
        snapshot = Snapshot("polish", "epoch", puzzle, clues.map { Cell(it) }, hostName = "Joseph", guestName = "Taylor"))
}
class MultiplayerPolishUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun bothCoopPlayersSeeTheSharedBoardAndKeepAuthoredNumbers() {
        val base = polishState()
        val host = Appearance(board = "linen", ink = "rose", font = "serif")
        val guest = Appearance(board = "midnight", ink = "cobalt", font = "mono")
        val initial = requireNotNull(base.snapshot)
        val cells = initial.cells.toMutableList().apply {
            this[2] = Cell(4, owner = HOST)
            this[3] = Cell(6, owner = GUEST)
        }
        val shared = initial.copy(cells = cells, hostAppearance = host, guestAppearance = guest)
        val state = mutableStateOf(base.copy(snapshot = shared, appearance = host))
        compose.setContent { SudokuTogetherTheme { PlayScreen(state.value, polishActions(), {}, {}, {}, {}) } }

        fun checkBoard(theme: String) {
            val pixels = compose.onNodeWithTag("cell_5").captureToImage().toPixelMap()
            assertEquals(boardFillFor(theme), pixels[pixels.width / 2, pixels.height / 2])
        }
        fun digitStyle(cell: Int, number: String): TextStyle {
            val layout = mutableListOf<TextLayoutResult>()
            compose.onNode(hasText(number) and hasAnyAncestor(hasTestTag("cell_$cell")), useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
            return layout.single().layoutInput.style
        }
        fun checkInk(cell: Int, ink: String) {
            val pixels = compose.onNodeWithTag("cell_$cell").captureToImage().toPixelMap()
            val expected = inkFor(ink, darkBoard = true)
            assertTrue("Cell $cell should use the $ink ink for a dark board", (0 until pixels.height).any { y ->
                (0 until pixels.width).any { x ->
                    val color = pixels[x, y]
                    abs(color.red - expected.red) < .05f && abs(color.green - expected.green) < .05f &&
                        abs(color.blue - expected.blue) < .05f
                }
            })
        }

        compose.onNodeWithText("Shared board · Midnight").assertIsDisplayed()
        checkBoard("midnight")
        assertEquals(fontFor("serif"), digitStyle(2, "4").fontFamily)
        assertEquals(fontFor("mono"), digitStyle(3, "6").fontFamily)
        checkInk(2, "rose")
        checkInk(3, "cobalt")
        compose.runOnIdle { state.value = state.value.copy(role = GUEST, appearance = guest, connected = false) }
        checkBoard("midnight")
        assertEquals(fontFor("serif"), digitStyle(2, "4").fontFamily)
        assertEquals(fontFor("mono"), digitStyle(3, "6").fontFamily)

        compose.runOnIdle { state.value = state.value.copy(uniformNumbers = true) }
        assertEquals(fontFor("clear"), digitStyle(2, "4").fontFamily)
        assertEquals(fontFor("clear"), digitStyle(3, "6").fontFamily)
        checkInk(2, "forest")
        checkInk(3, "forest")
        compose.runOnIdle { state.value = base.copy(snapshot = shared.copy(mode = VERSUS), appearance = host) }
        compose.onNodeWithText("Shared board · Midnight").assertDoesNotExist()
        checkBoard("linen")
    }

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
