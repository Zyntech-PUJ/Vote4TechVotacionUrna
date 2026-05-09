package com.vote4tech.urna.ui.votacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vote4tech.urna.data.remote.dto.CandidatoDto
import com.vote4tech.urna.ui.VotacionUiState
import com.vote4tech.urna.ui.VotacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotacionScreen(
    viewModel: VotacionViewModel,
    onCandidatoSeleccionado: () -> Unit
) {
    val candidatos by viewModel.candidatos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.ListoParaConfirmar) onCandidatoSeleccionado()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seleccione su Candidato") }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(candidatos) { candidato ->
                CandidatoItem(candidato) { viewModel.seleccionarCandidato(candidato) }
            }
        }
    }
}

@Composable
private fun CandidatoItem(candidato: CandidatoDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(candidato.nombre, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Puesto ${candidato.numero}", style = MaterialTheme.typography.bodySmall)
            candidato.nombrePartido?.let {
                Text("$it · ${candidato.siglaPartido ?: ""}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
