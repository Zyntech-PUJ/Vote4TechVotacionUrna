package com.vote4tech.urna.ui.confirmacion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vote4tech.urna.ui.VotacionUiState
import com.vote4tech.urna.ui.VotacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionScreen(
    viewModel: VotacionViewModel,
    onVotoRegistrado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.VotoRegistrado) {
            viewModel.reiniciar()
            onVotoRegistrado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Confirmar Voto") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is VotacionUiState.ListoParaConfirmar -> {
                    Text("Su voto va para:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Text(state.nombreCandidato, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(state.nombrePartido, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.confirmarVoto() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Confirmar y Votar") }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.reiniciar() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar") }
                }
                VotacionUiState.Cargando -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Registrando voto…")
                }
                is VotacionUiState.Error -> {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.confirmarVoto() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }
        }
    }
}
