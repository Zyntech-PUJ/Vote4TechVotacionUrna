package com.vote4tech.urna2.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Acceso de Registrador", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Para acceder a la configuración, un registrador electoral debe autenticarse.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

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
        Spacer(Modifier.height(8.dp))

        if (uiState is VotacionUiState.LoginRegistradorError) {
            Text(
                (uiState as VotacionUiState.LoginRegistradorError).mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

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
    }
}
