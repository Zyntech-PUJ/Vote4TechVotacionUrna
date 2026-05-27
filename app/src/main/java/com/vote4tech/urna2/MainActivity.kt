package com.vote4tech.urna2

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.vote4tech.urna2.ui.Routes
import com.vote4tech.urna2.ui.VotacionNavHost
import com.vote4tech.urna2.ui.VotacionViewModel
import com.vote4tech.urna2.ui.theme.Vote4TechVotacionUrna2Theme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: VotacionViewModel
    private lateinit var connectivityManager: ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModel.sincronizarAuto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UrnaApplication
        viewModel = ViewModelProvider(
            this,
            VotacionViewModel.Factory(app.prefs, app.database.votoDraftDao())
        )[VotacionViewModel::class.java]

        val startDestination = if (app.prefs.isConfigured) {
            viewModel.verificarDraftPendiente()
            Routes.IDENTIFICACION
        } else {
            Routes.CONFIG
        }

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        setContent {
            Vote4TechVotacionUrna2Theme {
                VotacionNavHost(
                    viewModel = viewModel,
                    prefs = app.prefs,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
    }
}
