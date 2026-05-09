package com.vote4tech.urna.ui.eleccion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vote4tech.urna.data.remote.dto.EleccionDto
import com.vote4tech.urna.ui.VotacionUiState
import com.vote4tech.urna.ui.VotacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EleccionScreen(
    viewModel: VotacionViewModel,
    onEleccionSeleccionada: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val elecciones by viewModel.elecciones.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarElecciones() }

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.CandidatosListos) onEleccionSeleccionada()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seleccione una Elección") }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                VotacionUiState.Cargando -> CircularProgressIndicator(Modifier.padding(32.dp))
                is VotacionUiState.Error -> Text(state.mensaje, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                else -> {
                    LazyColumn {
                        items(elecciones) { eleccion ->
                            EleccionItem(eleccion) { viewModel.seleccionarEleccion(eleccion) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EleccionItem(eleccion: EleccionDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(eleccion.nombre, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Tipo: ${eleccion.tipo}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
