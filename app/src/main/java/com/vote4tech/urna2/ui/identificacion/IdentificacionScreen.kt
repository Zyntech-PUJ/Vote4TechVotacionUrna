package com.vote4tech.urna2.ui.identificacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun IdentificacionScreen(
    viewModel: VotacionViewModel,
    onCiudadanoIdentificado: () -> Unit,
    onNavLoginRegistrador: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cedula by remember { mutableStateOf("") }

    // Diálogo acceso restringido domicilio
    if (uiState is VotacionUiState.CiudadanoDomicilio) {
        val nombre = (uiState as VotacionUiState.CiudadanoDomicilio).nombre
        AlertDialog(
            onDismissRequest = { viewModel.limpiarError(); cedula = "" },
            title = { Text("Acceso Restringido") },
            text = {
                Text(
                    "El ciudadano \"$nombre\" está habilitado para voto en domicilio.\n\n" +
                    "No puede votar en urna. Por favor contáctese con la Registraduría Nacional del Estado Civil para más información."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarError(); cedula = "" }) {
                    Text("Entendido")
                }
            }
        )
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is VotacionUiState.CiudadanoIdentificado -> {
                onCiudadanoIdentificado()
                viewModel.cargarElecciones()
            }
            else -> {}
        }
    }

    // Layout horizontal: izquierda = título+cédula+botón, derecha = configuración
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Panel principal ───────────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🗳️ Urna de Votación", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Ingrese su número de cédula para votar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

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
                    onSearch = {
                        if (cedula.length >= 6) viewModel.identificarCiudadano(cedula)
                    }
                ),
                isError = uiState is VotacionUiState.Error
            )

            if (uiState is VotacionUiState.Error) {
                val errorMsg = (uiState as VotacionUiState.Error).mensaje
                val esSinConexion = errorMsg.startsWith("Sin conexión")
                val icono = if (esSinConexion) "🔌" else "❌"
                val color = if (esSinConexion) Color(0xFFBF360C) else MaterialTheme.colorScheme.error
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(icono, style = MaterialTheme.typography.bodySmall)
                    Text(errorMsg, color = color, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (uiState is VotacionUiState.DraftPendiente) {
                val draft = uiState as VotacionUiState.DraftPendiente
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Votación en progreso:", style = MaterialTheme.typography.labelMedium)
                        Text(draft.nombre, style = MaterialTheme.typography.bodyLarge)
                        Text("Elección: ${draft.eleccion}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onCiudadanoIdentificado) { Text("Continuar") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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

        // ── Panel lateral: configuración ─────────────────────────────────────
        Column(
            modifier = Modifier.width(160.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onNavLoginRegistrador,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚙️ Configuración")
            }
        }
    }
}

