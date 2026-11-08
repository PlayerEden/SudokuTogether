package com.playereden.sudokutogether

data class Endpoint(val host: String, val port: Int)
fun parseAddress(text: String): Endpoint? {
    val value = text.trim()
    val host: String
    val portText: String
    if (value.startsWith("[")) {
        val end = value.indexOf("]:")
        if (end < 2) return null
        host = value.substring(1, end); portText = value.substring(end + 2)
        val parts = host.split('%')
        if (parts.size > 2 || !parts[0].matches(Regex("[0-9a-fA-F:.]+")) || parts[0].count { it == ':' } < 2) return null
        if (parts.size == 2 && !parts[1].matches(Regex("[a-zA-Z0-9_.-]+"))) return null
        if (runCatching { java.net.InetAddress.getByName(parts[0]) is java.net.Inet6Address }.getOrDefault(false).not()) return null
    } else {
        if (value.count { it == ':' } != 1) return null
        host = value.substringBefore(':'); portText = value.substringAfter(':')
        val numbers = host.split('.')
        if (numbers.size != 4 || numbers.any { !it.matches(Regex("[0-9]{1,3}")) || it.toInt() !in 0..255 }) return null
    }
    if (!portText.matches(Regex("[0-9]{1,5}"))) return null
    val port = portText.toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
    return Endpoint(host, port)
}
