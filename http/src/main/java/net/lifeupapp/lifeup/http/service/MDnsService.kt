package net.lifeupapp.lifeup.http.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import net.lifeupapp.lifeup.http.base.appCtx
import java.net.ServerSocket
import java.util.logging.Level
import java.util.logging.Logger

/**
 * MDnsService
 *
 * This class is used to register a service to the local network.
 * For the desktop app to discover the LifeUp Cloud.
 */
class MDnsService {

    private val logger = Logger.getLogger("MDnsService")
    private var hasRegistered = false

    private val ndsManager: NsdManager? by lazy {
        appCtx.getSystemService(Context.NSD_SERVICE) as NsdManager?
    }

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            // mServiceName = NsdServiceInfo.serviceName
            logger.info("registered service: ${NsdServiceInfo.serviceName}")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
        }
    }

    fun registerNsdService(port: Int) {
        if (hasRegistered) {
            return
        }
        try {
            // Create the NsdServiceInfo object, and populate it.
            val serviceInfo = NsdServiceInfo().apply {
                // The name is subject to change based on conflicts
                // with other services advertised on the same network.
                serviceName = "lifeup_cloud"
                serviceType = "_lifeup._tcp"
                setPort(getAvailablePort())
                setAttribute("port", port.toString())
            }

            ndsManager?.apply {
                registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD,
                    registrationListener
                )
            }
            hasRegistered = true
        } catch (t: Throwable) {
            logger.log(Level.SEVERE, "Failed to register NSD service", t)
        }
    }

    fun unregisterNsdService() {
        if (hasRegistered.not()) {
            return
        }
        try {
            ndsManager?.unregisterService(registrationListener)
        } catch (t: Throwable) {
            logger.log(Level.SEVERE, "Failed to unregister NSD service", t)
        }

        hasRegistered = false
    }

    fun getAvailablePort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port.also {
            logger.info("getAvailablePort: $it")
        }
    }
}
