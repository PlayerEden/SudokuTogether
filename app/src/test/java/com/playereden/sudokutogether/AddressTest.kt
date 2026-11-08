package com.playereden.sudokutogether

import org.junit.Assert.*
import org.junit.Test

class AddressTest {
    @Test fun localAddressHasAnExplicitValidPort() {
        assertEquals(Endpoint("192.168.1.24", 41421), parseAddress(" 192.168.1.24:41421 "))
        assertNull(parseAddress("192.168.1.24")); assertNull(parseAddress("192.168.1.24:0")); assertNull(parseAddress("192.168.1.24:65536"))
        assertNull(parseAddress("https://192.168.1.24:123")); assertNull(parseAddress("1.2.3.999:123")); assertNull(parseAddress("anything at all:123"))
    }
    @Test fun bracketedIpv6AddressesCanBeJoinedManually() {
        assertEquals(Endpoint("fe80::12%wlan0", 12345), parseAddress("[fe80::12%wlan0]:12345"))
    }
}
