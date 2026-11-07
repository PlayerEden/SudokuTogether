package com.playereden.sudokutogether

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.playereden.sudokutogether.core.Appearance

private val clearNumbers = FontFamily(
    Font(R.font.noto_sans_regular),
    Font(R.font.noto_sans_bold, weight = FontWeight.Bold)
)
private val bookishNumbers = FontFamily(Font(R.font.noto_serif_regular))
private val terminalNumbers = FontFamily(Font(R.font.liberation_mono_regular))
private val defaultAppearance = Appearance()

internal fun fontFor(id: String): FontFamily = when (id) {
    "serif" -> bookishNumbers
    "mono" -> terminalNumbers
    else -> clearNumbers
}

internal fun Appearance.withNumberDisplay(uniformNumbers: Boolean): Appearance =
    if (uniformNumbers) copy(ink = defaultAppearance.ink, font = defaultAppearance.font) else this
