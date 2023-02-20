package net.lifeupapp.lifeup.http.utils

import java.net.NetworkInterface

fun getIpAddressInLocalNetwork(): String? {
    val localAddresses = getIpAddressListInLocalNetwork()
    return localAddresses.firstOrNull()
}

fun getIpAddressListInLocalNetwork(): List<String> {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()?.iterator()?.asSequence()
    return networkInterfaces?.flatMap {
        it.inetAddresses.asSequence()
            .filter { inetAddress ->
                inetAddress.isSiteLocalAddress && inetAddress.isLoopbackAddress.not()
            }
            .map { inetAddress -> inetAddress.hostAddress }
    }?.toList() ?: emptyList()
}
