package com.playereden.sudokutogether

import android.util.JsonReader
import android.util.JsonToken
import com.playereden.sudokutogether.core.DailyChallenge
import com.playereden.sudokutogether.core.MAX_RECORDS
import com.playereden.sudokutogether.core.PuzzleSupply
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import java.io.Reader

/** Check token types and duplicate keys before Kotlin serialization can coerce them. */
@OptIn(ExperimentalSerializationApi::class)
internal object StrictBackupJson {
    private val integer = Regex("-?(0|[1-9][0-9]*)")

    fun header(input: Reader): Pair<String, Int> = reader(input).use {
        it.beginObject()
        var format: String? = null
        var schema: Int? = null
        while (it.hasNext()) {
            when (it.nextName()) {
                "format" -> { require(format == null); format = it.checkedString() }
                "schema" -> { require(schema == null); schema = it.checkedInteger().toInt() }
                else -> it.skipBounded()
            }
            if (format != null && schema != null) return@use format to schema
        }
        throw IllegalArgumentException("Missing backup format or schema.")
    }

    fun validate(input: Reader, descriptor: SerialDescriptor) = reader(input).use {
        it.verify(descriptor)
        require(it.peek() == JsonToken.END_DOCUMENT)
    }

    private fun reader(input: Reader) = JsonReader(input).apply { isLenient = false }

    private fun JsonReader.verify(descriptor: SerialDescriptor) {
        if (peek() == JsonToken.NULL) {
            require(descriptor.isNullable)
            nextNull()
            return
        }
        when (descriptor.kind) {
            StructureKind.CLASS -> {
                beginObject()
                val seen = BooleanArray(descriptor.elementsCount)
                while (hasNext()) {
                    val field = descriptor.getElementIndex(nextName())
                    require(field >= 0 && !seen[field])
                    seen[field] = true
                    verify(descriptor.getElementDescriptor(field))
                }
                endObject()
            }
            StructureKind.LIST -> {
                val item = descriptor.getElementDescriptor(0)
                // Bound collections before allocating the deserialized model.
                val limit = when (item.serialName.substringAfterLast('.')) {
                    "Int", "Cell" -> 81
                    "SoloUndo" -> 128
                    "DailyProgress" -> DailyChallenge.MAX_SAVED_DAYS
                    "GameRecord" -> MAX_RECORDS
                    "String" -> PuzzleSupply.RECENT_LIMIT
                    else -> error("Unexpected backup array type.")
                }
                beginArray()
                var count = 0
                while (hasNext()) {
                    require(++count <= limit)
                    verify(item)
                }
                endArray()
            }
            StructureKind.MAP -> {
                // The only map is Undo's related notes, keyed by cell index.
                require(descriptor.getElementDescriptor(0).kind == PrimitiveKind.INT)
                beginObject()
                val seen = mutableSetOf<Int>()
                while (hasNext()) {
                    val key = nextName()
                    val index = key.toInt()
                    require(seen.size < 20 && index in 0..80 && key == index.toString() && seen.add(index))
                    verify(descriptor.getElementDescriptor(1))
                }
                endObject()
            }
            PrimitiveKind.STRING -> checkedString()
            PrimitiveKind.BOOLEAN -> { require(peek() == JsonToken.BOOLEAN); nextBoolean() }
            PrimitiveKind.INT -> checkedInteger().toInt()
            PrimitiveKind.LONG -> checkedInteger().toLong()
            else -> error("Unexpected backup field type.")
        }
    }

    private fun JsonReader.checkedString(): String {
        require(peek() == JsonToken.STRING)
        return nextString().also { require(it.length <= 256) }
    }

    private fun JsonReader.checkedInteger(): String {
        require(peek() == JsonToken.NUMBER)
        return nextString().also { require(it.length <= 20 && integer.matches(it)) }
    }

    private fun JsonReader.skipBounded(depth: Int = 0) {
        require(depth < 16)
        when (peek()) {
            JsonToken.BEGIN_ARRAY -> { beginArray(); while (hasNext()) skipBounded(depth + 1); endArray() }
            JsonToken.BEGIN_OBJECT -> {
                beginObject()
                while (hasNext()) { nextName(); skipBounded(depth + 1) }
                endObject()
            }
            else -> skipValue()
        }
    }
}
