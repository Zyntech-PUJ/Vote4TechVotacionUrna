package com.vote4tech.urna2.ui.confirmacion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun ConfirmacionScreen(
    viewModel: VotacionViewModel,
    onVotoCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.VotoRegistrado) {
            viewModel.reiniciar()
            onVotoCompletado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is VotacionUiState.ListoParaConfirmar -> {
                Text("Confirmar Voto", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ha seleccionado:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.nombreCandidato, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        state.nombrePartido?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.confirmarVoto() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirmar y Votar")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.reiniciar(); onVotoCompletado() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
            is VotacionUiState.Cargando -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Registrando voto...")
            }
            is VotacionUiState.Error -> {
                Text(state.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.confirmarVoto() }) { Text("Reintentar") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.limpiarError() }) { Text("Volver") }
            }
            else -> {}
        }
    }
}
