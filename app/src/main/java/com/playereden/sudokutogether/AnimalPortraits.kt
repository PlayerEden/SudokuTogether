package com.playereden.sudokutogether

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
    "ocean_octopus" -> R.drawable.ocean_octopus
    "ocean_seal" -> R.drawable.ocean_seal
    "ocean_hermit_crab" -> R.drawable.ocean_hermit_crab
    "ocean_manta_ray" -> R.drawable.ocean_manta_ray
    "ocean_jellyfish" -> R.drawable.ocean_jellyfish
    "ocean_narwhal" -> R.drawable.ocean_narwhal
    else -> null
}

internal fun identityStyleName(appearance: Appearance): String {
    Cosmetics.oceanPortraits.firstOrNull { it.id == appearance.portrait }?.let { return "${it.name} · Ocean" }
    Cosmetics.woodlandPortraits.firstOrNull { it.id == appearance.portrait }?.let { return "${it.name} · Woodland" }
    return "${emblemName(appearance.emblem)} · Basic"
}

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
