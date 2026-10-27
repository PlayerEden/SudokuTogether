package com.playereden.sudokutogether.core

import java.net.Socket
import java.net.InetSocketAddress
import java.io.IOException
import java.util.concurrent.Executors

class GuestClient(private val socketFactory: () -> Socket = { Socket() }, private val listener: (ClientEvent) -> Unit = {}) : AutoCloseable {
    private val reader = Executors.newSingleThreadExecutor(threads("sudoku-input"))
    @Volatile private var link: Link? = null
    private fun emit(event: ClientEvent) { runCatching { listener(event) } }
    fun connect(host: String, port: Int, code: String, name: String, resumeToken: String? = null, appearance: Appearance = Appearance()) {
        require(Cosmetics.valid(appearance))
        if (host.isBlank() || port !in 1..65535) throw IOException("Enter a valid host address and port.")
        check(link == null) { "Already connected" }
        val socket = socketFactory()
        try {
            socket.connect(InetSocketAddress(host, port), 5000); socket.soTimeout = 8000; socket.tcpNoDelay = true
            Protocol.write(socket.getOutputStream(), Message("hello", code = code, name = cleanName(name, "Partner"), token = resumeToken, appearance = appearance))
            val welcome = Protocol.read(socket.getInputStream())
            if (welcome.type == "error") throw IOException(welcome.text.take(300))
            if (welcome.type != "welcome" || welcome.version != 3 || welcome.snapshot == null || welcome.status == null || welcome.token == null) throw IOException("The host is running an incompatible app version.")
            validateSnapshot(welcome.snapshot)
            if (welcome.token.length !in 32..128) throw IOException("Invalid rejoin credential")
            socket.soTimeout = 10_000
            val current = Link(socket) { drop(it, "Connection paused. Reconnect to the host to continue.") }; link = current
            emit(ClientEvent(welcome.snapshot, welcome.status, welcome.token, history = welcome.lines))
            reader.execute {
                var latest: Snapshot = welcome.snapshot
                try {
                    while (!socket.isClosed) {
                        val message = Protocol.read(socket.getInputStream()); current.lastRead = System.nanoTime()
                        if (message.version != 3) throw IOException("Install the same app version on both phones.")
                        when (message.type) {
                            "state" -> {
                                val snapshot = message.snapshot ?: throw IOException("Missing puzzle")
                                val status = message.status ?: throw IOException("Missing room state")
                                validateSnapshot(snapshot)
                                if (snapshot.sessionId != latest.sessionId || (snapshot.epoch == latest.epoch && snapshot.revision < latest.revision)) throw IOException("Out-of-order puzzle state")
                                latest = snapshot; emit(ClientEvent(snapshot, status, outcome = message.outcome))
                            }
                            "select" -> {
                                if (message.cell !in -1..80) throw IOException("Invalid selection")
                                emit(ClientEvent(selected = message.cell))
                            }
                            "chat" -> {
                                val line = message.line ?: throw IOException("Invalid chat message")
                                if (line.player !in listOf(HOST, GUEST) || line.text.isBlank()) throw IOException("Invalid chat sender")
                                emit(ClientEvent(chat = line))
                            }
                            "ping" -> current.send(Message("pong"))
                            "error" -> throw IOException(message.text.take(300))
                            else -> throw IOException("Unsupported game message")
                        }
                    }
                } catch (_: Exception) { drop(current, "Connection paused. Reconnect to the host to continue.") }
            }
        } catch (e: Exception) { socket.close(); throw if (e is IOException) e else IOException("Could not join this room.", e) }
    }
    private fun drop(current: Link, reason: String) {
        synchronized(this) {
            if (link !== current) return
            link = null; current.close(); emit(ClientEvent(disconnected = true, message = reason))
        }
    }
    fun submit(operation: Operation) {
        val current = link ?: throw IOException("Reconnect before entering a move.")
        current.send(Message("move", operation = operation))
    }
    fun select(cell: Int) { if (cell in -1..80) link?.send(Message("select", cell = cell)) }
    fun sendChat(text: String) {
        val content = ChatRules.clean(text)
        if (content.isNotEmpty()) (link ?: throw IOException("Reconnect to chat.")).send(Message("chat", text = content))
    }
    fun changeAppearance(appearance: Appearance) {
        require(Cosmetics.valid(appearance))
        (link ?: throw IOException("Reconnect to update your style.")).send(Message("style", appearance = appearance))
    }
    fun setForeground(active: Boolean) { link?.send(Message("presence", active = active)) }
    override fun close() {
        synchronized(this) { val current = link; link = null; current?.close(); reader.shutdownNow() }
    }
}
