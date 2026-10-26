package com.playereden.sudokutogether.core

import kotlinx.serialization.Serializable
import java.io.InputStream
import java.io.OutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.ThreadFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class RoomStatus(val hostActive: Boolean = true, val guestActive: Boolean = false, val reserved: Boolean = false) {
    val paused: Boolean get() = !hostActive || (reserved && !guestActive)
}
@Serializable data class Message(
    val type: String, val version: Int = 2, val code: String = "", val name: String = "", val token: String? = null,
    val snapshot: Snapshot? = null, val status: RoomStatus? = null, val operation: Operation? = null,
    val outcome: Outcome? = null, val cell: Int = -1, val active: Boolean = true, val text: String = ""
)
data class HostEvent(val snapshot: Snapshot, val status: RoomStatus, val partnerCell: Int = -1, val outcome: Outcome? = null, val error: String? = null)
data class ClientEvent(val snapshot: Snapshot? = null, val status: RoomStatus? = null, val token: String? = null, val selected: Int? = null, val outcome: Outcome? = null, val disconnected: Boolean = false, val message: String? = null)
object Protocol {
    private val json = Json { encodeDefaults = true }
    fun read(input: InputStream): Message {
        try {
            val data = DataInputStream(input); val size = data.readInt()
            if (size !in 1..131072) throw IOException("Invalid message size")
            val bytes = ByteArray(size); data.readFully(bytes)
            return json.decodeFromString<Message>(bytes.toString(Charsets.UTF_8)).also {
                if (it.type.length !in 1..24 || it.name.length > 100 || it.code.length > 32 || (it.token?.length ?: 0) > 128) throw IOException("Invalid message")
            }
        } catch (e: IOException) { throw e } catch (e: Exception) { throw IOException("Unreadable game message", e) }
    }
    fun write(output: OutputStream, message: Message) {
        val bytes = json.encodeToString(message).toByteArray(Charsets.UTF_8)
        if (bytes.size !in 1..131072) throw IOException("Game message is too large")
        DataOutputStream(output).apply { writeInt(bytes.size); write(bytes); flush() }
    }
}

internal fun threads(name: String) = ThreadFactory { runnable -> Thread(runnable, name).apply { isDaemon = true } }
internal class Link(val socket: Socket, private val failed: (Link) -> Unit) : AutoCloseable {
    @Volatile var lastRead = System.nanoTime()
    private val writer = ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, ArrayBlockingQueue(32), threads("sudoku-output"))
    fun send(message: Message) {
        try { writer.execute { try { Protocol.write(socket.getOutputStream(), message) } catch (_: Exception) { failed(this) } } }
        catch (_: java.util.concurrent.RejectedExecutionException) { failed(this) }
    }
    override fun close() { runCatching { socket.close() }; writer.shutdownNow() }
}
