package com.vote4tech.urna.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vote4tech.urna.ui.config.ConfigScreen
import com.vote4tech.urna.ui.confirmacion.ConfirmacionScreen
import com.vote4tech.urna.ui.eleccion.EleccionScreen
import com.vote4tech.urna.ui.identificacion.IdentificacionScreen
import com.vote4tech.urna.ui.votacion.VotacionScreen

object Routes {
    const val CONFIG       = "config"
    const val IDENTIFICACION = "identificacion"
    const val ELECCION     = "eleccion"
    const val VOTACION     = "votacion"
    const val CONFIRMACION = "confirmacion"
}

@Composable
fun VotacionNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    viewModel: VotacionViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.CONFIG) {
            ConfigScreen(onGuardado = {
                navController.navigate(Routes.IDENTIFICACION) {
                    popUpTo(Routes.CONFIG) { inclusive = true }
                }
            })
        }

        composable(Routes.IDENTIFICACION) {
            IdentificacionScreen(
                viewModel = viewModel,
                onIdentificado = { navController.navigate(Routes.ELECCION) }
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
                onVotoRegistrado = {
                    navController.navigate(Routes.IDENTIFICACION) {
                        popUpTo(Routes.IDENTIFICACION) { inclusive = true }
                    }
                }
            )
        }
    }
}
