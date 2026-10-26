package com.playereden.sudokutogether

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.ArrayDeque

data class NearbyRoom(val name: String, val host: String, val port: Int, val sessionId: String = "")

class RoomDiscovery(context: Context, private val onError: (String) -> Unit) {
    private val app = context.applicationContext
    private val manager = app.getSystemService(NsdManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val multicast = (app.getSystemService(Context.WIFI_SERVICE) as WifiManager).createMulticastLock("sudoku-discovery").apply { setReferenceCounted(false) }
    private var registration: NsdManager.RegistrationListener? = null
    private var discovery: NsdManager.DiscoveryListener? = null
    private val pending = ArrayDeque<NsdServiceInfo>()
    private val rooms = linkedMapOf<String, NearbyRoom>()
    private var resolving = false
    private var generation = 0
    private var callback: ((List<NearbyRoom>) -> Unit)? = null
    private val type = "_sudokut._tcp."

    fun startHosting(name: String, port: Int, sessionId: String) = handler.post {
        stopInternal()
        try {
            multicast.acquire()
            val listener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(info: NsdServiceInfo) = Unit
                override fun onServiceUnregistered(info: NsdServiceInfo) = Unit
                override fun onRegistrationFailed(info: NsdServiceInfo, error: Int) { onError("Nearby discovery is unavailable. Your partner can join using the address below.") }
                override fun onUnregistrationFailed(info: NsdServiceInfo, error: Int) = Unit
            }
            registration = listener
            manager.registerService(NsdServiceInfo().apply {
                serviceName = "${name.take(20)}'s puzzle"; serviceType = type; setPort(port)
                setAttribute("v", "1"); setAttribute("session", sessionId)
            }, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) { onError("Nearby discovery is unavailable. Your partner can use the manual address.") }
    }

    fun discover(onRooms: (List<NearbyRoom>) -> Unit) = handler.post {
        stopInternal(); callback = onRooms
        val current = generation
        try {
            multicast.acquire()
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) = Unit
                override fun onDiscoveryStopped(serviceType: String) = Unit
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) { onError("Couldn't search nearby rooms. Enter the host's address instead.") }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
                override fun onServiceFound(info: NsdServiceInfo) { handler.post {
                    if (current == generation && info.serviceType.startsWith("_sudokut._tcp")) { pending.add(info); resolveNext(current) }
                } }
                override fun onServiceLost(info: NsdServiceInfo) { handler.post {
                    if (current == generation) { rooms.remove(info.serviceName); callback?.invoke(rooms.values.toList()) }
                } }
            }
            discovery = listener; manager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) { onError("Nearby discovery is unavailable. Enter the host's address instead.") }
    }

    @Suppress("DEPRECATION")
    private fun resolveNext(current: Int) {
        if (current != generation || resolving || pending.isEmpty()) return
        resolving = true
        val info = pending.removeFirst()
        fun finished() { if (current == generation) { resolving = false; resolveNext(current) } }
        try {
            manager.resolveService(info, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) { handler.post { finished() } }
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) { handler.post {
                    if (current == generation) {
                        val host = serviceInfo.host?.hostAddress
                        val version = serviceInfo.attributes["v"]?.toString(Charsets.UTF_8)
                        if (host != null && serviceInfo.port in 1..65535 && version == "1") {
                            rooms[serviceInfo.serviceName] = NearbyRoom(serviceInfo.serviceName, host, serviceInfo.port, serviceInfo.attributes["session"]?.toString(Charsets.UTF_8) ?: "")
                            callback?.invoke(rooms.values.toList())
                        }
                        finished()
                    }
                } }
            })
        } catch (_: Exception) { finished() }
    }

    fun stop() = handler.post { stopInternal() }
    private fun stopInternal() {
        generation++; pending.clear(); rooms.clear(); resolving = false; callback = null
        discovery?.let { runCatching { manager.stopServiceDiscovery(it) } }; discovery = null
        registration?.let { runCatching { manager.unregisterService(it) } }; registration = null
        if (multicast.isHeld) runCatching { multicast.release() }
    }

    fun localAddresses(): List<String> {
        val connectivity = app.getSystemService(ConnectivityManager::class.java)
        val wifi = connectivity.allNetworks.filter { connectivity.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true }
            .flatMap { connectivity.getLinkProperties(it)?.linkAddresses.orEmpty() }.map { it.address }
            .filterIsInstance<Inet4Address>().filterNot { it.isLoopbackAddress }.mapNotNull { it.hostAddress }
        if (wifi.isNotEmpty()) return wifi.distinct()
        return runCatching { NetworkInterface.getNetworkInterfaces().toList().filter { it.name.startsWith("wlan") || it.name.startsWith("eth") }
            .flatMap { it.inetAddresses.toList() }.filterIsInstance<Inet4Address>().filterNot { it.isLoopbackAddress }.mapNotNull { it.hostAddress }.distinct() }.getOrDefault(emptyList())
    }
}
