package com.playereden.sudokutogether

import androidx.compose.ui.text.font.FontListFontFamily
import com.playereden.sudokutogether.core.Appearance
import com.playereden.sudokutogether.core.Cosmetics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberAppearanceTest {
    @Test fun savedFontIdsResolveToDistinctPackagedFamilies() {
        val families = Cosmetics.fonts.associate { it.id to fontFor(it.id) }

        for ((id, family) in families) {
            assertTrue("$id should resolve to a bundled font family", family is FontListFontFamily)
        }
        assertEquals(Cosmetics.fonts.size, families.values.toSet().size)
    }

    @Test fun unrecognizedFontUsesClear() {
        assertEquals(fontFor(Appearance().font), fontFor("unrecognized_font"))
    }

    @Test fun classicDisplayOnlyOverridesTheShownFontAndInk() {
        val equipped = Appearance(
            ink = "rose", board = "linen", font = "serif", emblem = "leaf", portrait = "woodland_fox"
        )
        assertEquals(equipped.copy(ink = "forest", font = "clear"), equipped.withNumberDisplay(true))
        assertEquals(equipped, equipped.withNumberDisplay(false))
    }
}
