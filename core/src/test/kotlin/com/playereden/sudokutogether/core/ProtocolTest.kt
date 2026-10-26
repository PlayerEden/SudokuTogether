package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test
import java.io.*

class ProtocolTest {
    @Test fun roundTripReadsSeparateFramesWithoutConsumingTheNext() {
        val bytes = ByteArrayOutputStream()
        Protocol.write(bytes, Message("select", cell = 4)); Protocol.write(bytes, Message("ping"))
        val input = ByteArrayInputStream(bytes.toByteArray())
        assertEquals(4, Protocol.read(input).cell); assertEquals("ping", Protocol.read(input).type)
    }
    @Test fun rejectsNegativeOversizedTruncatedAndMalformedFrames() {
        for (length in listOf(-1, 0, 131073)) {
            val bytes = ByteArrayOutputStream().also { DataOutputStream(it).writeInt(length) }
            assertThrows(IOException::class.java) { Protocol.read(ByteArrayInputStream(bytes.toByteArray())) }
        }
        val truncated = ByteArrayOutputStream().also { DataOutputStream(it).apply { writeInt(5); writeByte(1) } }
        assertThrows(IOException::class.java) { Protocol.read(ByteArrayInputStream(truncated.toByteArray())) }
        val malformed = ByteArrayOutputStream().also { DataOutputStream(it).apply { writeInt(1); writeByte(123) } }
        assertThrows(IOException::class.java) { Protocol.read(ByteArrayInputStream(malformed.toByteArray())) }
    }
}
