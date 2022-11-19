package net.lifeupapp.lifeup.http.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import net.lifeupapp.lifeup.http.base.appCtx

object ConnectStatusManager {

    private val connectivityManager by lazy {
        appCtx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    val networkChangedEvent = MutableSharedFlow<Unit>(replay = 1)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    Log.i("ConnectStatusManager", "onCapabilitiesChanged")
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    networkChangedEvent.tryEmit(Unit)
                }
            })
        } else {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        networkChangedEvent.tryEmit(Unit)
                    }
                }
            )
        }
    }
}