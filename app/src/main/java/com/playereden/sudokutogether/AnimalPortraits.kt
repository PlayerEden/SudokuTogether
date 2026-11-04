package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.playereden.sudokutogether.core.*

private fun animalDrawable(id: String): Int? = when (id) {
    "woodland_owl" -> R.drawable.woodland_owl
    "woodland_fox" -> R.drawable.woodland_fox
    "woodland_rabbit" -> R.drawable.woodland_rabbit
    "woodland_bear" -> R.drawable.woodland_bear
    "woodland_badger" -> R.drawable.woodland_badger
    "woodland_hedgehog" -> R.drawable.woodland_hedgehog
    "woodland_robin" -> R.drawable.woodland_robin
    "woodland_deer" -> R.drawable.woodland_deer
    "woodland_squirrel" -> R.drawable.woodland_squirrel
    "woodland_otter" -> R.drawable.woodland_otter
    "woodland_tortoise" -> R.drawable.woodland_tortoise
    "woodland_cat" -> R.drawable.woodland_cat
    else -> null
}

internal fun identityStyleName(appearance: Appearance): String =
    Cosmetics.portraits.firstOrNull { it.id == appearance.portrait }?.let { "${it.name} · Woodland" }
        ?: emblemName(appearance.emblem)

@Composable internal fun AnimalPortrait(appearance: Appearance, modifier: Modifier = Modifier.size(48.dp)) {
    val drawable = animalDrawable(appearance.portrait)
    BoxWithConstraints(modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp, maxWidth = 96.dp, maxHeight = 96.dp)
        .clip(CircleShape).background(GreenTint)
        .testTag("portrait_image_${appearance.portrait.ifEmpty { appearance.emblem }}"), contentAlignment = Alignment.Center) {
        if (drawable != null) Image(painterResource(drawable), contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(maxWidth * .08f), contentScale = ContentScale.Fit)
        else Text(emblemSymbol(appearance.emblem), color = inkFor(appearance.ink, false),
            fontSize = (minOf(maxWidth.value, maxHeight.value) * .64f / LocalDensity.current.fontScale.coerceAtLeast(1f)).sp)
    }
}

@Composable internal fun WoodlandCollection(state: GameUiState, actions: GameActions) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Woodland", color = Forest, fontFamily = FontFamily.Serif, fontSize = 22.sp, modifier = Modifier.weight(1f))
            if (state.appearance.portrait.isNotEmpty()) TextButton({ actions.cosmetic("emblem", state.appearance.emblem) },
                modifier = Modifier.heightIn(min = 48.dp)) { Text("Use basic icon", fontSize = 13.sp) }
        }
        val available = Cosmetics.portraits.count { Cosmetics.allowed(it.id, state.profile.records) }
        Text("$available of ${Cosmetics.portraits.size} available", color = Muted, fontSize = 13.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 300.dp && LocalDensity.current.fontScale <= 1.3f) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Cosmetics.portraits.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item ->
                            val unlocked = Cosmetics.allowed(item.id, state.profile.records)
                            val chosen = state.appearance.portrait == item.id
                            Surface(onClick = { actions.cosmetic("portrait", item.id) }, enabled = unlocked && !state.busy,
                                modifier = Modifier.weight(1f).testTag("portrait_${item.id}").semantics {
                                    selected = chosen
                                    stateDescription = if (chosen) "Selected" else if (unlocked) "Available" else "Locked. ${item.requirement}"
                                }, shape = RoundedCornerShape(17.dp), color = if (chosen) GreenTint else Paper,
                                border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) Forest else Rule)) {
                                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AnimalPortrait(state.appearance.copy(portrait = item.id), Modifier.size(72.dp))
                                    Text(item.name, color = Forest, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text(if (chosen) "Selected" else if (unlocked) "Choose" else item.requirement,
                                        color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
