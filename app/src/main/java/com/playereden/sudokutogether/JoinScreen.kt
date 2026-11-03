package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

@Composable internal fun JoinScreen(state: GameUiState, actions: GameActions) {
    var address by rememberSaveable(state.address) { mutableStateOf(state.address) }
    var code by rememberSaveable { mutableStateOf("") }
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val codeFocus = remember { FocusRequester() }
    val canJoin = !state.busy && parseAddress(address) != null && (state.resumingGuest || code.length == 6)
    fun join() { if (canJoin) parseAddress(address)?.let { focus.clearFocus(); actions.join(it, code) } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            JournalHeader(if (state.resumingGuest) "Rejoin room" else "Join room", "Same Wi-Fi", actions.home)
            GamePanel {
                SectionTitle("Nearby rooms")
                if (state.rooms.isEmpty()) Row(Modifier.fillMaxWidth().background(Cream, RoundedCornerShape(12.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    GameIcon(GameSymbol.GRID, Muted, Modifier.size(20.dp))
                    Text("No nearby rooms yet.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                }
                state.rooms.forEach { room ->
                    val formatted = if (room.host.contains(':')) "[${room.host}]:${room.port}" else "${room.host}:${room.port}"
                    val chosen = address == formatted
                    Surface(onClick = { address = formatted; codeFocus.requestFocus() }, enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth().semantics { selected = chosen }, shape = RoundedCornerShape(13.dp),
                        color = if (chosen) GreenTint else Paper, border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) Forest else Rule)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GameIcon(GameSymbol.PEOPLE)
                            Text(room.name, color = Forest, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            GameIcon(if (chosen) GameSymbol.CHECK else GameSymbol.RIGHT, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                OutlinedTextField(address, { address = it }, label = { Text("Host address") },
                    placeholder = { Text("192.168.1.24:41421") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { codeFocus.requestFocus() }), shape = RoundedCornerShape(13.dp))
                OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) },
                    label = { Text(if (state.resumingGuest) "Room code (optional)" else "Room code") },
                    singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(codeFocus), shape = RoundedCornerShape(13.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { join() }))
                GamePrimaryButton(if (state.busy) "Connecting…" else "Join room", ::join, canJoin, modifier = Modifier.testTag("join_room"))
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.message?.let { Notice(it) }
                if (state.resumingGuest) TextButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth()) { Text("Use a new code") }
            }
            TextButton(onClick = { helpOpen = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Connection help") }

        }
    }
    if (helpOpen) AlertDialog(onDismissRequest = { helpOpen = false },
        title = { Text("Connection help") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Connect both phones to the same Wi-Fi and keep the host's app open.")
                Text("For manual joining, enter the address and port from Room details, then the six-digit code.")
                Text("Some guest networks block connections between phones.")
            }
        },
        confirmButton = { TextButton(onClick = { helpOpen = false }) { Text("Got it") } })
}
