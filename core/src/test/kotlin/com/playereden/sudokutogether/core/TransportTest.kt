package com.playereden.sudokutogether.core

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class TransportTest {
    private fun <T> await(queue: LinkedBlockingQueue<T>, predicate: (T) -> Boolean): T {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8)
        while (System.nanoTime() < deadline) { val item = queue.poll(100, TimeUnit.MILLISECONDS); if (item != null && predicate(item)) return item }
        throw AssertionError("Expected network event did not arrive")
    }
    @Test fun hostAndGuestShareRealEditsAndDuplicateRequestsDoNotRepeat() {
        val hostEvents = LinkedBlockingQueue<HostEvent>(); val events = LinkedBlockingQueue<ClientEvent>()
        HostServer(GameSession.create(fixture()), hostEvents::offer).use { server ->
            val port = server.start()
            GuestClient(events::offer).use { guest ->
                guest.connect("127.0.0.1", port, server.code, "Partner")
                val joined = await(events) { it.token != null }.snapshot!!
                assertEquals("Partner", joined.guestName)
                assertTrue(server.submitLocal(joined.move(0)).accepted)
                val afterHost = await(events) { it.snapshot?.cells?.get(0)?.value == 1 }.snapshot!!
                val operation = afterHost.move(1, number = 2)
                guest.submit(operation)
                assertEquals(2, await(events) { it.outcome?.operationId == operation.id }.snapshot!!.cells[1].value)
                guest.submit(operation)
                assertEquals(ResultCode.DUPLICATE, await(events) { it.outcome?.code == ResultCode.DUPLICATE }.outcome!!.code)
                assertEquals(2L, server.session.snapshot().revision)
            }
        }
    }
    @Test fun sameCellContentionRejectsAStaleNoteWithoutOverwriting() {
        val events = LinkedBlockingQueue<ClientEvent>()
        HostServer(GameSession.create(fixture())).use { host ->
            val port = host.start()
            GuestClient(events::offer).use { guest ->
                guest.connect("127.0.0.1", port, host.code, "Partner")
                val start = await(events) { it.token != null }.snapshot!!
                host.submitLocal(start.move(0))
                guest.submit(start.move(0, Kind.NOTE, 9))
                assertEquals(ResultCode.STALE, await(events) { it.outcome?.code == ResultCode.STALE }.outcome!!.code)
                assertEquals(1, host.session.snapshot().cells[0].value)
            }
        }
    }
    @Test fun wrongCodesFullRoomsAndProtocolMismatchesAreRejected() {
        HostServer(GameSession.create(fixture())).use { host ->
            val port = host.start()
            GuestClient().use { wrong -> assertThrows(IOException::class.java) { wrong.connect("127.0.0.1", port, "wrong", "No") } }
            Socket("127.0.0.1", port).use { socket ->
                Protocol.write(socket.getOutputStream(), Message("hello", version = 999, code = host.code))
                assertEquals("error", Protocol.read(socket.getInputStream()).type)
            }
            GuestClient().use { first ->
                first.connect("127.0.0.1", port, host.code, "Partner")
                GuestClient().use { second -> assertThrows(IOException::class.java) { second.connect("127.0.0.1", port, host.code, "Extra") } }
            }
        }
    }
    @Test fun backgroundPresencePausesAndResumesBothPlayers() {
        val events = LinkedBlockingQueue<ClientEvent>(); val hosts = LinkedBlockingQueue<HostEvent>()
        HostServer(GameSession.create(fixture()), hosts::offer).use { host ->
            val port = host.start()
            GuestClient(events::offer).use { guest ->
                guest.connect("127.0.0.1", port, host.code, "Partner"); await(events) { it.token != null }
                guest.setForeground(false)
                await(hosts) { it.status.reserved && it.status.paused }
                assertEquals(ResultCode.PAUSED, host.submitLocal(host.session.snapshot().move(0)).code)
                guest.setForeground(true); await(events) { it.status?.guestActive == true && it.status.paused.not() }
                host.setForeground(false); await(events) { it.status?.hostActive == false }
                host.setForeground(true); await(events) { it.status?.hostActive == true && it.status.guestActive }
                assertTrue(host.submitLocal(host.session.snapshot().move(0)).accepted)
            }
        }
    }
    @Test fun unconsumedAcknowledgmentSurvivesHostReloadAndTokenRejoin() {
        val game = GameSession.create(fixture()); var token: String; var saved: SavedSession
        val hosts = LinkedBlockingQueue<HostEvent>()
        HostServer(game, hosts::offer).use { host ->
            val port = host.start()
            Socket("127.0.0.1", port).use { raw ->
                Protocol.write(raw.getOutputStream(), Message("hello", code = host.code, name = "Partner"))
                val welcome = Protocol.read(raw.getInputStream()); token = welcome.token!!
                val op = welcome.snapshot!!.move(0)
                Protocol.write(raw.getOutputStream(), Message("move", operation = op))
                await(hosts) { it.snapshot.cells[0].value == 1 }
                saved = game.saved()
                // Close without reading the saved move's acknowledgment.
            }
        }
        val events = LinkedBlockingQueue<ClientEvent>()
        HostServer(GameSession(saved)).use { restored ->
            val port = restored.start()
            GuestClient(events::offer).use { guest ->
                guest.connect("127.0.0.1", port, "", "Partner", token)
                val rejoined = await(events) { it.token != null }.snapshot!!
                assertEquals(1, rejoined.cells[0].value); assertEquals(1L, rejoined.revision)
            }
        }
    }
    @Test fun replacingPartnerInvalidatesTheirCredentialAndNewPuzzlesRejectOldMoves() {
        val events = LinkedBlockingQueue<ClientEvent>()
        HostServer(GameSession.create(fixture())).use { host ->
            val port = host.start(); var token: String
            GuestClient(events::offer).use { guest ->
                guest.connect("127.0.0.1", port, host.code, "Partner")
                val welcome = await(events) { it.token != null }; token = welcome.token!!
                val old = welcome.snapshot!!.move(0)
                host.newPuzzle(234)
                await(events) { it.snapshot?.epoch != null && it.snapshot.epoch != old.epoch }
                guest.submit(old)
                assertEquals(ResultCode.WRONG_GAME, await(events) { it.outcome?.code == ResultCode.WRONG_GAME }.outcome!!.code)
                host.replaceGuest()
                await(events) { it.disconnected }
            }
            GuestClient().use { old -> assertThrows(IOException::class.java) { old.connect("127.0.0.1", port, host.code, "Partner", token) } }
        }
    }
}
