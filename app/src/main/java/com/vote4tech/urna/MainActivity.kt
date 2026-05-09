package com.vote4tech.urna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vote4tech.urna.data.local.entity.EstadoDraft
import com.vote4tech.urna.ui.Routes
import com.vote4tech.urna.ui.VotacionNavHost
import com.vote4tech.urna.ui.VotacionViewModel
import com.vote4tech.urna.ui.theme.VotacionUrnaTheme
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UrnaApplication

        val viewModel: VotacionViewModel by viewModels {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    VotacionViewModel(app.prefs, app.database.votoDraftDao()) as T
            }
        }

        setContent {
            VotacionUrnaTheme {
                val navController = rememberNavController()

                // Determinar pantalla de inicio
                val startDest = if (app.prefs.isConfigured) Routes.IDENTIFICACION else Routes.CONFIG

                VotacionNavHost(
                    navController = navController,
                    startDestination = startDest,
                    viewModel = viewModel
                )
            }
        }

        // Detectar si hay un voto en borrador y navegar a la pantalla correspondiente
        // (se hace en onCreate para hacerlo antes de que el usuario vea cualquier pantalla)
        viewModel.verificarDraftPendiente { estado ->
            // El navController no está listo aquí; la verificación
            // se refleja en el estado del ViewModel y cada pantalla
            // lo maneja apropiadamente al coleccionar uiState.
        }
    }
}
