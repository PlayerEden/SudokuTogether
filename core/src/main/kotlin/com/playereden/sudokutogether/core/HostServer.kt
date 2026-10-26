package com.playereden.sudokutogether.core

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class HostServer(val session: GameSession, private val listener: (HostEvent) -> Unit = {}) : AutoCloseable {
    val code: String = SecureRandom().nextInt(1_000_000).toString().padStart(6, '0')
    private val lock = Any()
    private val workers = Executors.newFixedThreadPool(4, threads("sudoku-peer"))
    private val acceptor = Executors.newSingleThreadExecutor(threads("sudoku-accept"))
    private val heartbeat = Executors.newSingleThreadScheduledExecutor(threads("sudoku-heartbeat"))
    private val admissions = Semaphore(4)
    private var socket: ServerSocket? = null
    private var peer: Link? = null
    private var hostActive = true
    private var guestActive = false
    private var partnerCell = -1
    private var closed = false
    private val failures = mutableMapOf<String, MutableList<Long>>()
    private fun status() = RoomStatus(hostActive, peer != null && guestActive, session.snapshot().guestName != null)
    private fun syncPresence() { session.setPresence(hostActive, peer != null && guestActive) }
    private fun emit(outcome: Outcome? = null) { runCatching { listener(HostEvent(session.snapshot(), status(), partnerCell, outcome)) } }
    private fun publish(outcome: Outcome? = null) {
        syncPresence()
        peer?.send(Message("state", snapshot = session.snapshot(), status = status(), outcome = outcome))
        emit(outcome)
    }
    fun start(): Int = synchronized(lock) {
        check(!closed) { "Room is closed" }
        socket?.let { return@synchronized it.localPort }
        val server = ServerSocket(0); socket = server
        acceptor.execute {
            while (!server.isClosed) {
                val incoming = try { server.accept() } catch (_: IOException) { break }
                if (!admissions.tryAcquire()) { incoming.close(); continue }
                workers.execute { try { handle(incoming) } finally { admissions.release() } }
            }
        }
        heartbeat.scheduleAtFixedRate({
            val current = synchronized(lock) { peer }
            if (current != null) {
                if (System.nanoTime() - current.lastRead > TimeUnit.SECONDS.toNanos(10)) drop(current)
                else current.send(Message("ping"))
            }
        }, 2, 2, TimeUnit.SECONDS)
        publish(); server.localPort
    }
    private fun handle(incoming: Socket) {
        var admitted: Link? = null
        try {
            incoming.soTimeout = 5000; incoming.tcpNoDelay = true
            val hello = Protocol.read(incoming.getInputStream())
            synchronized(lock) {
                fun reject(text: String): Nothing { Protocol.write(incoming.getOutputStream(), Message("error", text = text)); throw IOException(text) }
                if (closed) reject("This room has closed.")
                if (hello.type != "hello" || hello.version != 1) reject("Install the same app version on both phones.")
                if (peer != null) reject("This room already has two players.")
                val address = incoming.inetAddress.hostAddress ?: "local"
                val now = System.currentTimeMillis()
                if (failures.size > 32) failures.clear()
                val attempts = failures.getOrPut(address) { mutableListOf() }.also { it.removeAll { time -> now - time > 60_000 } }
                if (attempts.size >= 5) reject("Too many incorrect codes. Try again in a minute.")
                if (hello.token == null && hello.code != code) { attempts.add(now); reject("That room code is incorrect.") }
                val identity = try { session.admitGuest(hello.name, hello.token) } catch (e: Exception) { reject(e.message ?: "Could not join this room.") }
                incoming.soTimeout = 10_000
                val link = Link(incoming, ::drop); admitted = link; peer = link; guestActive = true; partnerCell = -1; syncPresence()
                Protocol.write(incoming.getOutputStream(), Message("welcome", snapshot = session.snapshot(), status = status(), token = identity.token))
                emit()
            }
            val link = admitted!!
            while (!incoming.isClosed) {
                val message = Protocol.read(incoming.getInputStream()); link.lastRead = System.nanoTime()
                synchronized(lock) {
                    if (peer !== link) throw IOException("Room connection changed")
                    if (message.version != 1) throw IOException("Incompatible game message")
                    when (message.type) {
                        "move" -> {
                            val operation = message.operation ?: throw IOException("Missing move")
                            publish(session.submit(GUEST, operation))
                        }
                        "select" -> {
                            if (message.cell !in -1..80) throw IOException("Invalid selection")
                            partnerCell = if (guestActive) message.cell else -1; emit()
                        }
                        "presence" -> { guestActive = message.active; if (!guestActive) partnerCell = -1; publish() }
                        "pong" -> Unit
                        else -> throw IOException("Unsupported game message")
                    }
                }
            }
        } catch (_: Exception) { /* Drop the connection; confirmed state remains in the session. */ }
        finally { admitted?.let(::drop) ?: runCatching { incoming.close() } }
    }
    private fun drop(link: Link) = synchronized(lock) {
        link.close()
        if (peer === link) { peer = null; guestActive = false; partnerCell = -1; publish() }
    }
    fun submitLocal(operation: Operation): Outcome = synchronized(lock) {
        syncPresence(); session.submit(HOST, operation).also { publish(it) }
    }
    fun select(cell: Int) = synchronized(lock) {
        if (cell in -1..80) peer?.send(Message("select", cell = if (hostActive) cell else -1))
        Unit
    }
    fun setForeground(active: Boolean) = synchronized(lock) {
        hostActive = active
        if (!active) peer?.send(Message("select", cell = -1))
        publish()
    }
    fun replaceGuest() = synchronized(lock) {
        session.replaceGuest()
        val previous = peer; peer = null; guestActive = false; partnerCell = -1
        previous?.close(); publish()
    }
    fun newPuzzle(seed: Long) {
        val puzzle = Sudoku.generate(seed)
        synchronized(lock) { session.newPuzzle(puzzle); partnerCell = -1; peer?.send(Message("select", cell = -1)); publish() }
    }
    override fun close() = synchronized(lock) {
        if (!closed) {
            closed = true; hostActive = false
            runCatching { socket?.close() }; peer?.close(); peer = null
            heartbeat.shutdownNow(); workers.shutdownNow(); acceptor.shutdownNow(); syncPresence(); emit()
        }
        Unit
    }
}
