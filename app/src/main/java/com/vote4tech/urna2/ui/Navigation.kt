package com.vote4tech.urna2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vote4tech.urna2.ui.conexion.ConexionServidorScreen
import com.vote4tech.urna2.ui.config.ConfigScreen
import com.vote4tech.urna2.ui.confirmacion.ConfirmacionScreen
import com.vote4tech.urna2.ui.dashboard.DashboardScreen
import com.vote4tech.urna2.ui.eleccion.EleccionScreen
import com.vote4tech.urna2.ui.identificacion.IdentificacionScreen
import com.vote4tech.urna2.ui.instrucciones.InstruccionesScreen
import com.vote4tech.urna2.ui.login.LoginRegistradorScreen
import com.vote4tech.urna2.ui.huella.HuellaScreen
import com.vote4tech.urna2.ui.votacion.VotacionScreen
import com.vote4tech.urna2.util.PrefsManager

object Routes {
    const val CONFIG = "config"
    const val CONEXION_SERVIDOR = "conexion_servidor"
    const val IDENTIFICACION = "identificacion"
    const val HUELLA = "huella"
    const val ELECCION = "eleccion"
    const val VOTACION = "votacion"
    const val CONFIRMACION = "confirmacion"
    const val LOGIN_REGISTRADOR = "login_registrador"
    const val DASHBOARD = "dashboard"
    const val INSTRUCCIONES = "instrucciones"
}

@Composable
fun VotacionNavHost(
    viewModel: VotacionViewModel,
    prefs: PrefsManager,
    startDestination: String
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.KickedPorServidor) {
            viewModel.reiniciar()
            navController.navigate(Routes.CONFIG) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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
                onCiudadanoIdentificado = { navController.navigate(Routes.HUELLA) },
                onNavLoginRegistrador = {
                    navController.navigate(Routes.LOGIN_REGISTRADOR)
                }
            )
        }
        composable(Routes.HUELLA) {
            HuellaScreen(
                viewModel = viewModel,
                onSiguiente = {
                    navController.navigate(Routes.ELECCION) {
                        popUpTo(Routes.HUELLA) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN_REGISTRADOR) {
            LoginRegistradorScreen(
                viewModel = viewModel,
                onLoginExito = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN_REGISTRADOR) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavConexion = {
                    navController.navigate(Routes.CONEXION_SERVIDOR)
                },
                onNavInstrucciones = {
                    navController.navigate(Routes.INSTRUCCIONES)
                },
                onBack = {
                    viewModel.limpiarError()
                    navController.navigate(Routes.IDENTIFICACION) {
                        popUpTo(Routes.IDENTIFICACION) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CONEXION_SERVIDOR) {
            ConexionServidorScreen(
                viewModel = viewModel,
                prefs = prefs,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.INSTRUCCIONES) {
            InstruccionesScreen(
                onBack = { navController.popBackStack() }
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
