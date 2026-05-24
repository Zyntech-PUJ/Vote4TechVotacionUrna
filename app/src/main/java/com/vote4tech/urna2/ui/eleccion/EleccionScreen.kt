package com.vote4tech.urna2.ui.eleccion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        viewModel.volverAElecciones()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Panel izquierdo ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "\uD83D\uDDF3\uFE0F Elecciones",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Seleccione la elección en la que desea votar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Panel derecho ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
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
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarElecciones() }) { Text("Reintentar") }
                    }
                }
                is VotacionUiState.YaVoto -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                    "\u26A0\uFE0F Ya votó en esta elección",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Este ciudadano ya registró su voto en:\n${state.nombreEleccion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.cargarElecciones() }) {
                                    Text("Volver a la lista de elecciones")
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
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
            Text(eleccion.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Tipo: ${eleccion.tipo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
