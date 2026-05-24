package com.vote4tech.urna2.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build

/**
 * Utilidad para conectarse al hotspot del servidor.
 *
 * Android 10+: usa [ConnectivityManager.requestNetwork] con [WifiNetworkSpecifier].
 * El sistema muestra un diálogo pidiéndole al usuario que confirme la conexión.
 * Una vez aceptado, se vincula el proceso a esa red con [ConnectivityManager.bindProcessToNetwork]
 * para que todas las peticiones HTTP de Retrofit vayan por el hotspot (sin internet).
 *
 * Android <10: usa [android.net.wifi.WifiConfiguration] (deprecated pero funcional).
 */
object WifiConnectHelper {

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Solicita conexión al hotspot.
     * @param onConnected invocado cuando la red está disponible.
     * @param onFailed    invocado si el usuario cancela o la conexión falla.
     */
    fun connectToHotspot(
        context: Context,
        ssid: String,
        password: String,
        onConnected: (Network?) -> Unit,
        onFailed: () -> Unit
    ) {
        if (ssid.isBlank()) { onFailed(); return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectApi29(context, ssid, password, onConnected, onFailed)
        } else {
            connectLegacy(context, ssid, password)
            onConnected(null)
        }
    }

    @Suppress("NewApi")
    private fun connectApi29(
        context: Context,
        ssid: String,
        password: String,
        onConnected: (Network?) -> Unit,
        onFailed: () -> Unit
    ) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Cancelar callback previo si existe
        networkCallback?.let {
            try { cm.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }

        val specBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
        if (password.isNotBlank()) specBuilder.setWpa2Passphrase(password)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specBuilder.build())
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Vincula el proceso a esta red para que Retrofit la use
                cm.bindProcessToNetwork(network)
                onConnected(network)
            }
            override fun onUnavailable() {
                onFailed()
            }
        }
        networkCallback = callback
        cm.requestNetwork(request, callback)
    }

    @Suppress("DEPRECATION")
    private fun connectLegacy(context: Context, ssid: String, password: String) {
        try {
            val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            val config = android.net.wifi.WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$password\""
            }
            val netId = wm.addNetwork(config)
            if (netId != -1) {
                wm.disconnect()
                wm.enableNetwork(netId, true)
                wm.reconnect()
            }
        } catch (_: Exception) {}
    }

    /** Libera el binding de red (llamar al salir de la app si se desea). */
    fun releaseNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let {
            try { cm.unregisterNetworkCallback(it) } catch (_: Exception) {}
            networkCallback = null
        }
        try { cm.bindProcessToNetwork(null) } catch (_: Exception) {}
    }
}
