package com.vote4tech.urna2.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun LoginRegistradorScreen(
    viewModel: VotacionViewModel,
    onLoginExito: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.LoginRegistradorExito) {
            viewModel.limpiarError()
            onLoginExito()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Panel izquierdo: descripción ──────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔐", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Acceso de\nRegistrador o Jurado",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ingresa tus credenciales de registrador o jurado para acceder al panel de administración.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // ── Panel derecho: formulario ─────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState is VotacionUiState.LoginRegistradorError
            )
            if (uiState is VotacionUiState.LoginRegistradorError) {
                val errMsg = (uiState as VotacionUiState.LoginRegistradorError).mensaje
                val esSinConexion = errMsg.startsWith("Sin conexión")
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(if (esSinConexion) "🔌" else "🔑", style = MaterialTheme.typography.bodySmall)
                    Text(
                        errMsg,
                        color = if (esSinConexion) Color(0xFFBF360C) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.loginRegistrador(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank() && uiState !is VotacionUiState.Cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is VotacionUiState.Cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.limpiarError(); /* navController.popBackStack via callback not needed here */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
