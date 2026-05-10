package com.vote4tech.urna2.ui.votacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.data.remote.dto.CandidatoDto
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun VotacionScreen(
    viewModel: VotacionViewModel,
    onCandidatoSeleccionado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.ListoParaConfirmar) {
            onCandidatoSeleccionado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Seleccione su Candidato", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            is VotacionUiState.CandidatosListos -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.candidatos) { candidato ->
                        CandidatoItem(
                            candidato = candidato,
                            onClick = { viewModel.seleccionarCandidato(candidato) }
                        )
                    }
                }
            }
            is VotacionUiState.Cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is VotacionUiState.Error -> {
                Text(state.mensaje, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}

@Composable
private fun CandidatoItem(candidato: CandidatoDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(candidato.numero, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(candidato.nombre, style = MaterialTheme.typography.titleMedium)
                candidato.nombrePartido?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
