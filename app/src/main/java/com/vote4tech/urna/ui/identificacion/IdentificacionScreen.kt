package com.vote4tech.urna.ui.identificacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vote4tech.urna.ui.VotacionUiState
import com.vote4tech.urna.ui.VotacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentificacionScreen(
    viewModel: VotacionViewModel,
    onIdentificado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cedula by remember { mutableStateOf("") }

    // Navegar cuando el ciudadano es identificado correctamente
    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.CiudadanoIdentificado) {
            onIdentificado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Identificación del Votante") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ingrese su número de cédula",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it.filter { c -> c.isDigit() } },
                label = { Text("Número de cédula") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(24.dp))

            when (val state = uiState) {
                is VotacionUiState.Error -> {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.limpiarError() }) { Text("Reintentar") }
                }
                VotacionUiState.Cargando -> CircularProgressIndicator()
                else -> {}
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.identificarCiudadano(cedula.trim()) },
                enabled = cedula.length >= 6 && uiState !is VotacionUiState.Cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verificar")
            }
        }
    }
}
