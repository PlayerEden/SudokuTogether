package com.playereden.sudokutogether

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.playereden.sudokutogether.core.DailyChallenge
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyCompatibilityTest {
    @Test fun localDateWorksOnTheMinimumSupportedAndroidVersion() {
        assertEquals("2026-11-02", DailyChallenge.today(zone = ZoneId.of("America/Chicago"), instant = Instant.parse("2026-11-03T02:00:00Z")))
    }
    @Test fun challengeDateChangesAtLocalMidnight() {
        val zone = ZoneId.of("America/Chicago")
        assertEquals("2026-11-02", DailyChallenge.today(zone = zone, instant = Instant.parse("2026-11-03T05:59:59Z")))
        assertEquals("2026-11-03", DailyChallenge.today(zone = zone, instant = Instant.parse("2026-11-03T06:00:00Z")))
    }
}
