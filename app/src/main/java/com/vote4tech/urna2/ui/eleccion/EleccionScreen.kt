package com.vote4tech.urna2.ui.eleccion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.data.remote.dto.EleccionDto
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun EleccionScreen(
    viewModel: VotacionViewModel,
    onEleccionSeleccionada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.CandidatosListos) {
            onEleccionSeleccionada()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState is VotacionUiState.CiudadanoIdentificado || uiState is VotacionUiState.Idle) {
            viewModel.cargarElecciones()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Seleccione una Elección", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            is VotacionUiState.Cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is VotacionUiState.EleccionesListas -> {
                if (state.elecciones.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay elecciones activas en este momento")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.elecciones) { eleccion ->
                            EleccionItem(
                                eleccion = eleccion,
                                onClick = { viewModel.seleccionarEleccion(eleccion) }
                            )
                        }
                    }
                }
            }
            is VotacionUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.cargarElecciones() }) { Text("Reintentar") }
                }
            }
            is VotacionUiState.YaVoto -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠ Ya votó en esta elección",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Este ciudadano ya registró su voto en:\n${state.nombreEleccion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.cargarElecciones() }) {
                        Text("Volver a la lista de elecciones")
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun EleccionItem(eleccion: EleccionDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(eleccion.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Tipo: ${eleccion.tipo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
