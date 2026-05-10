package com.vote4tech.urna2.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.data.remote.RetrofitClient
import com.vote4tech.urna2.util.PrefsManager

@Composable
fun ConfigScreen(
    prefs: PrefsManager,
    onConfigGuardada: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(prefs.serverUrl.ifBlank { "http://10.0.2.2:8081" }) }
    var idMesaStr by remember { mutableStateOf(if (prefs.idMesa > 0) prefs.idMesa.toString() else "1") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configuración de Mesa", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it; error = null },
            label = { Text("URL del Servidor") },
            placeholder = { Text("http://10.0.2.2:8081") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = idMesaStr,
            onValueChange = { idMesaStr = it.filter { c -> c.isDigit() }; error = null },
            label = { Text("ID de Mesa") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val idMesa = idMesaStr.toLongOrNull()
                when {
                    serverUrl.isBlank() -> error = "La URL del servidor es requerida"
                    idMesa == null || idMesa <= 0 -> error = "ID de mesa inválido"
                    else -> {
                        prefs.serverUrl = serverUrl.trim()
                        prefs.idMesa = idMesa
                        RetrofitClient.init(serverUrl.trim())
                        onConfigGuardada()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y Continuar")
        }
    }
}
