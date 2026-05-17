package com.vote4tech.urna2.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vote4tech.urna2.ui.config.ConfigScreen
import com.vote4tech.urna2.ui.confirmacion.ConfirmacionScreen
import com.vote4tech.urna2.ui.eleccion.EleccionScreen
import com.vote4tech.urna2.ui.identificacion.IdentificacionScreen
import com.vote4tech.urna2.ui.login.LoginRegistradorScreen
import com.vote4tech.urna2.ui.votacion.VotacionScreen
import com.vote4tech.urna2.util.PrefsManager

object Routes {
    const val CONFIG = "config"
    const val IDENTIFICACION = "identificacion"
    const val ELECCION = "eleccion"
    const val VOTACION = "votacion"
    const val CONFIRMACION = "confirmacion"
    const val LOGIN_REGISTRADOR = "login_registrador"
}

@Composable
fun VotacionNavHost(
    viewModel: VotacionViewModel,
    prefs: PrefsManager,
    startDestination: String
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.CONFIG) {
            ConfigScreen(
                prefs = prefs,
                onConfigGuardada = {
                    navController.navigate(Routes.IDENTIFICACION) {
                        popUpTo(Routes.CONFIG) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.IDENTIFICACION) {
            IdentificacionScreen(
                viewModel = viewModel,
                onCiudadanoIdentificado = { navController.navigate(Routes.ELECCION) },
                onNavConfig = {
                    navController.navigate(Routes.CONFIG) {
                        popUpTo(Routes.IDENTIFICACION) { inclusive = false }
                    }
                },
                onNavLoginRegistrador = {
                    navController.navigate(Routes.LOGIN_REGISTRADOR)
                },
                onConfigClick = {
                    if (prefs.isConfigured) {
                        viewModel.verificarConectividadParaConfig()
                    } else {
                        navController.navigate(Routes.CONFIG) {
                            popUpTo(Routes.IDENTIFICACION) { inclusive = false }
                        }
                    }
                }
            )
        }
        composable(Routes.LOGIN_REGISTRADOR) {
            LoginRegistradorScreen(
                viewModel = viewModel,
                onLoginExito = {
                    navController.navigate(Routes.CONFIG) {
                        popUpTo(Routes.LOGIN_REGISTRADOR) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ELECCION) {
            EleccionScreen(
                viewModel = viewModel,
                onEleccionSeleccionada = { navController.navigate(Routes.VOTACION) }
            )
        }
        composable(Routes.VOTACION) {
            VotacionScreen(
                viewModel = viewModel,
                onCandidatoSeleccionado = { navController.navigate(Routes.CONFIRMACION) }
            )
        }
        composable(Routes.CONFIRMACION) {
            ConfirmacionScreen(
                viewModel = viewModel,
                onVotoCompletado = {
                    navController.navigate(Routes.IDENTIFICACION) {
                        popUpTo(Routes.IDENTIFICACION) { inclusive = true }
                    }
                },
                onVotarEnOtraEleccion = {
                    navController.navigate(Routes.ELECCION) {
                        popUpTo(Routes.ELECCION) { inclusive = true }
                    }
                }
            )
        }
    }
}

