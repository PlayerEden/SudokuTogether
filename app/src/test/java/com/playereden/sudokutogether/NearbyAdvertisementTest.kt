package com.playereden.sudokutogether

import com.playereden.sudokutogether.core.WIRE_PROTOCOL_VERSION
import org.junit.Assert.*
import org.junit.Test

class NearbyAdvertisementTest {
    private fun attributes(flag: String? = null, version: Int = WIRE_PROTOCOL_VERSION) =
        buildMap { put("v",version.toString().toByteArray());put("session","saved-game".toByteArray());if (flag != null) put("locked",flag.toByteArray()) }
    @Test fun missingAndUnrecognizedAccessFlagsStayProtected() {
        for (flag in listOf(null,"","false","1","unknown"))
            assertTrue(decodeNearbyRoom("Taylor's game","192.168.1.20",41421,attributes(flag))!!.requiresCode)
        assertFalse(decodeNearbyRoom("Taylor's game","192.168.1.20",41421,attributes("0"))!!.requiresCode)
    }
    @Test fun incompatibleVersionsAndMissingEndpointsAreNotOffered() {
        assertNull(decodeNearbyRoom("Old game","192.168.1.20",41421,attributes("0",5)))
        assertNull(decodeNearbyRoom("No address",null,41421,attributes("0")))
        assertNull(decodeNearbyRoom("No port","192.168.1.20",0,attributes("0")))
    }
    @Test fun validDiscoveryRetainsTheNameAddressAndRejoinSession() {
        val room=decodeNearbyRoom("Taylor's game","fe80::2",41421,attributes("0"))!!
        assertEquals("Taylor's game",room.name);assertEquals("fe80::2",room.host)
        assertEquals(41421,room.port);assertEquals("saved-game",room.sessionId);assertFalse(room.requiresCode)
    }
}
