package com.vote4tech.urna2.ui.identificacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun IdentificacionScreen(
    viewModel: VotacionViewModel,
    onCiudadanoIdentificado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cedula by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.CiudadanoIdentificado) {
            onCiudadanoIdentificado()
            viewModel.cargarElecciones()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Identificación del Votante", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = cedula,
            onValueChange = {
                cedula = it.filter { c -> c.isDigit() }
                if (uiState is VotacionUiState.Error) viewModel.limpiarError()
            },
            label = { Text("Número de Cédula") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { if (cedula.length >= 6) viewModel.identificarCiudadano(cedula) }
            ),
            isError = uiState is VotacionUiState.Error
        )
        Spacer(Modifier.height(8.dp))

        if (uiState is VotacionUiState.Error) {
            Text(
                (uiState as VotacionUiState.Error).mensaje,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
        }

        if (uiState is VotacionUiState.DraftPendiente) {
            val draft = uiState as VotacionUiState.DraftPendiente
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Votación en progreso:", style = MaterialTheme.typography.labelMedium)
                    Text(draft.nombre, style = MaterialTheme.typography.bodyLarge)
                    Text("Elección: ${draft.eleccion}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onCiudadanoIdentificado) { Text("Continuar") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.identificarCiudadano(cedula) },
            enabled = cedula.length >= 6 && uiState !is VotacionUiState.Cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is VotacionUiState.Cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Verificar")
            }
        }
    }
}
