package com.homeworkout.app.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object NetworkMonitor {

    var isOffline by mutableStateOf(true)
        private set

    val isOnline: Boolean
        get() = !isOffline

    private var started = false
    private var connectivityManager: ConnectivityManager? = null

    fun start(context: Context) {

        if (started) {
            return
        }

        started = true

        connectivityManager =
            context.applicationContext
                .getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

        updateStatus()

        connectivityManager?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(
                    network: Network
                ) {
                    updateStatus()
                }

                override fun onLost(
                    network: Network
                ) {
                    updateStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    updateStatus()
                }
            }
        )
    }

    private fun updateStatus() {

        val manager =
            connectivityManager
                ?: return

        val network =
            manager.activeNetwork

        val capabilities =
            manager.getNetworkCapabilities(
                network
            )

        isOffline =
            capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) != true
    }
}
