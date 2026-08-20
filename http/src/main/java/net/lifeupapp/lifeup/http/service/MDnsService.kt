package net.lifeupapp.lifeup.http.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import net.lifeupapp.lifeup.http.base.appCtx
import net.lifeupapp.lifeup.http.utils.getIpAddressInLocalNetwork
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Advertise LifeUp Cloud on the LAN as `_lifeup._tcp` / `lifeup_cloud`.
 * SRV port and TXT `port` are the real HTTP port. TXT `ipv4` is a hint when mDNS address records lag.
 */
class MDnsService {

    private val logger = Logger.getLogger("MDnsService")
    private var hasRegistered = false

    private val ndsManager: NsdManager? by lazy {
        appCtx.getSystemService(Context.NSD_SERVICE) as NsdManager?
    }

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            logger.info("registered service: ${serviceInfo.serviceName} port=${serviceInfo.port}")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            logger.warning("NSD registration failed: code=$errorCode name=${serviceInfo.serviceName}")
            hasRegistered = false
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            logger.info("unregistered service: ${arg0.serviceName}")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            logger.warning("NSD unregistration failed: code=$errorCode")
        }
    }

    fun registerNsdService(port: Int) {
        if (hasRegistered) return
        val manager = ndsManager ?: run {
            logger.warning("NsdManager unavailable")
            return
        }
        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "lifeup_cloud"
                serviceType = "_lifeup._tcp"
                setPort(port)
                setAttribute("port", port.toString())
                getIpAddressInLocalNetwork()?.let { setAttribute("ipv4", it) }
            }
            manager.registerService(
                serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener,
            )
            hasRegistered = true
        } catch (t: Throwable) {
            hasRegistered = false
            logger.log(Level.SEVERE, "Failed to register NSD service", t)
        }
    }

    fun unregisterNsdService() {
        if (hasRegistered.not()) return
        try {
            ndsManager?.unregisterService(registrationListener)
        } catch (t: Throwable) {
            logger.log(Level.SEVERE, "Failed to unregister NSD service", t)
        }
        hasRegistered = false
    }
}
