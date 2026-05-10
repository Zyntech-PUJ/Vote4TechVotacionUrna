package com.vote4tech.urna2

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UrnaApplication
        val viewModel = ViewModelProvider(
            this,
            VotacionViewModel.Factory(app.prefs, app.database.votoDraftDao())
        )[VotacionViewModel::class.java]

        val startDestination = if (app.prefs.isConfigured) {
            viewModel.verificarDraftPendiente()
            Routes.IDENTIFICACION
        } else {
            Routes.CONFIG
        }

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
}
