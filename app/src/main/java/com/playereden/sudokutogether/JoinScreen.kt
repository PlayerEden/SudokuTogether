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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

@Composable internal fun JoinScreen(state: GameUiState, actions: GameActions) {
    var address by rememberSaveable(state.address) { mutableStateOf(state.address) }
    var manualOpen by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    var protectedAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var protectedName by rememberSaveable { mutableStateOf("") }
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val manualCodeFocus = remember { FocusRequester() }
    val protectedCodeFocus = remember { FocusRequester() }
    fun connect(endpoint: Endpoint, entered: String) { focus.clearFocus();actions.join(endpoint, entered) }
    fun manualJoin() {
        if (!state.busy && (code.isEmpty() || code.length == 6)) parseAddress(address)?.let { connect(it, code) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            JournalHeader(if (state.resumingGuest) "Rejoin game" else "Join a game", "Same Wi-Fi", actions.home)
            GamePanel {
                if (state.resumingGuest && parseAddress(state.address) != null) {
                    GamePrimaryButton(if (state.busy) "Connecting…" else "Rejoin game",
                        { parseAddress(state.address)?.let { connect(it, "") } }, !state.busy)
                }
                SectionTitle("Nearby games")
                if (state.rooms.isEmpty()) Text("Ask your partner to start a game.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                state.rooms.forEach { room ->
                    Surface(onClick = {
                        if (room.requiresCode && !state.resumingGuest) {
                            protectedAddress = if (room.host.contains(':')) "[${room.host}]:${room.port}" else "${room.host}:${room.port}"
                            protectedName = room.name;code = ""
                        } else connect(Endpoint(room.host, room.port), "")
                    }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(14.dp), color = Paper, border = BorderStroke(1.dp, Rule)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GameIcon(GameSymbol.PEOPLE)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(room.name, color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(if (state.resumingGuest) "Tap to rejoin" else if (room.requiresCode) "Code required" else "Tap to join",
                                    color = Muted, fontSize = 13.sp)
                            }
                            GameIcon(if (room.requiresCode && !state.resumingGuest) GameSymbol.LOCK else GameSymbol.RIGHT, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.message?.let { Notice(it) }
                TextButton(onClick = { actions.showJoin(state.resumingGuest) }, enabled = !state.busy,
                    modifier = Modifier.heightIn(min = 48.dp)) { Text("Refresh games") }
                if (state.resumingGuest) TextButton(onClick = { actions.showJoin(false) }, enabled = !state.busy,
                    modifier = Modifier.heightIn(min = 48.dp)) { Text("Join a different game") }
            }
            TextButton(onClick = { manualOpen = !manualOpen }, enabled = !state.busy,
                modifier = Modifier.heightIn(min = 48.dp)) { Text(if (manualOpen) "Hide manual entry" else "Join manually") }
            if (manualOpen) GamePanel {
                OutlinedTextField(address, { address = it }, label = { Text("Host address") },
                    placeholder = { Text("192.168.1.24:41421") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("manual_address"), enabled = !state.busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { manualCodeFocus.requestFocus() }), shape = RoundedCornerShape(13.dp))
                OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("Code (if needed)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(manualCodeFocus), enabled = !state.busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { manualJoin() }), shape = RoundedCornerShape(13.dp))
                GamePrimaryButton(if (state.busy) "Connecting…" else "Join game", ::manualJoin,
                    !state.busy && parseAddress(address) != null && (code.isEmpty() || code.length == 6), modifier = Modifier.testTag("join_room"))
            }
            TextButton(onClick = { helpOpen = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Connection help") }
        }
    }
    if (protectedAddress != null) {
        fun joinProtected() {
            if (!state.busy && code.length == 6) protectedAddress?.let(::parseAddress)?.let { connect(it, code) }
        }
        AlertDialog(onDismissRequest = { if (!state.busy) protectedAddress = null }, title = { Text("Enter game code") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(protectedName, color = Muted)
                    OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("Six-digit code") },
                        singleLine = true, enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth().focusRequester(protectedCodeFocus).testTag("nearby_code"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { joinProtected() }))
                    LaunchedEffect(protectedAddress) { protectedCodeFocus.requestFocus() }
                    state.message?.let { Notice(it) }
                }
            },
            confirmButton = { TextButton(::joinProtected, enabled = !state.busy && code.length == 6,
                modifier = Modifier.heightIn(min = 48.dp).testTag("nearby_join")) { Text(if (state.busy) "Connecting…" else "Join game") } },
            dismissButton = { TextButton({ protectedAddress = null }, enabled = !state.busy, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancel") } })
    }
    if (helpOpen) AlertDialog(onDismissRequest = { helpOpen = false }, title = { Text("Connection help") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Connect both phones to the same Wi-Fi. Your partner starts a game; you tap it here.")
                Text("If it doesn't appear, choose Join manually and enter the address from Game details. Enter its code if one is required.")
                Text("Keep the host's app open. Some guest networks block connections between phones.")
            }
        }, confirmButton = { TextButton({ helpOpen = false }) { Text("Got it") } })
}
