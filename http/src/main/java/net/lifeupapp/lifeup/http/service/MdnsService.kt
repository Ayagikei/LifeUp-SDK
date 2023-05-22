package net.lifeupapp.lifeup.http.service

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lifeupapp.lifeup.http.base.appCtx
import net.lifeupapp.lifeup.http.vo.MdnsInfo
import java.net.InetAddress
import java.net.UnknownHostException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

/**
 * register a mdns service
 */
class MdnsService(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "MdnsService"
    }

    private val wifiManager = appCtx.getSystemService(
        Context.WIFI_SERVICE
    ) as WifiManager

    private val lock = wifiManager.createMulticastLock("lifeup-cloud")
    private var jmDns: JmDNS? = null
    private val mutex = Mutex()

    fun register(port: String) {
        scope.launch {
            try {
                mutex.lock()
                if (jmDns != null) {
                    unregister()
                }

                lock.setReferenceCounted(false)
                lock.acquire()

                val addr: InetAddress? = intToInetAddress(wifiManager.connectionInfo.ipAddress)
                if (addr == null) {
                    unregister()
                }

                Log.i(TAG, "local host: ${InetAddress.getLocalHost().hostName}")

                val jmdns = JmDNS.create(addr).also {
                    this@MdnsService.jmDns = it
                }
                // Register a service
                val serviceInfo: ServiceInfo = ServiceInfo.create(
                    "_http._tcp.local.",
                    "lifeup_cloud",
                    "",
                    4242,
                    0,
                    0,
                    true,
                    Json.encodeToString(MdnsInfo(port))
                )
                jmdns.registerService(serviceInfo)

                Log.i(TAG, "jmdns Service registered")
            } catch (e: Exception) {
                Log.e(TAG, "failed to register jmdns", e)
                unregister()
            } finally {
                mutex.unlock()
            }
        }
    }

    fun unregister() {
        scope.launch {
            mutex.withLock {
                try {
                    if (lock.isHeld) {
                        lock.release()
                    }
                    jmDns?.unregisterAllServices()
                    jmDns?.close()
                    jmDns = null
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    private fun intToInetAddress(hostAddress: Int): InetAddress? {
        val inetAddress: InetAddress
        val addressBytes = byteArrayOf(
            (0xff and hostAddress).toByte(),
            (0xff and (hostAddress shr 8)).toByte(),
            (0xff and (hostAddress shr 16)).toByte(),
            (0xff and (hostAddress shr 24)).toByte()
        )
        inetAddress = try {
            InetAddress.getByAddress(addressBytes)
        } catch (e: UnknownHostException) {
            return null
        }
        return inetAddress
    }
}