package com.playereden.sudokutogether

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playereden.sudokutogether.core.GUEST
import com.playereden.sudokutogether.core.Snapshot

@Composable internal fun VersusPresence(state: GameUiState, snapshot: Snapshot, reconnect: () -> Unit) {
    val presence = versusPresenceState(snapshot, state.role, state.status, state.connected)
    key(snapshot.sessionId, snapshot.epoch) {
        Column(
            Modifier.widthIn(max = 456.dp).fillMaxWidth().testTag("versus_presence"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                presence.phase.label, color = Forest, fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp, lineHeight = 16.sp,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VersusLane("You", presence.self, presence.target, Forest, GreenTint, false,
                    Modifier.weight(1f).testTag("versus_you"))
                VersusLane("Rival", presence.rival, presence.target, Gold, GoldTint,
                    presence.phase == VersusPhase.LIVE, Modifier.weight(1f).testTag("versus_rival"))
            }
            if (state.role == GUEST && !state.connected) {
                TextButton(
                    onClick = reconnect, enabled = !state.busy,
                    modifier = Modifier.align(Alignment.End).heightIn(min = 48.dp)
                ) { Text("Reconnect") }
            }
        }
    }
}

@Composable private fun VersusLane(
    label: String,
    player: VersusPlayer?,
    target: Int,
    accent: Color,
    tint: Color,
    animate: Boolean,
    modifier: Modifier
) {
    Surface(
        modifier.semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(14.dp), color = if (player == null) Paper else tint,
        border = BorderStroke(1.dp, Rule)
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (player != null) {
                    AnimalPortrait(player.appearance, Modifier.size(32.dp).clearAndSetSemantics {})
                } else {
                    Box(Modifier.size(32.dp).background(Cream, CircleShape), contentAlignment = Alignment.Center) {
                        GameIcon(GameSymbol.PERSON, Muted, Modifier.size(18.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(label, color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
                    Text(player?.name ?: "Open seat", color = Ink, fontSize = 13.sp, lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(
                if (player == null) "Join over Wi-Fi" else "${player.filled} of $target filled",
                color = Ink, fontSize = 11.sp, lineHeight = 14.sp
            )
            val fraction = if (player == null || target == 0) 0f else player.filled.toFloat() / target
            val fill = if (animate) animateFloatAsState(fraction, tween(200), label = "Rival progress").value else fraction
            Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Rule)) {
                if (fill > 0f) Box(Modifier.fillMaxWidth(fill).fillMaxHeight().background(accent))
            }
        }
    }
}
