package com.vote4tech.urna.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vote4tech.urna.data.remote.RetrofitClient
import com.vote4tech.urna.util.PrefsManager

/**
 * Pantalla de configuración inicial.
 * El administrador ingresa la URL del servidor LAN y el número de mesa.
 * Se muestra solo cuando la app no está configurada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(onGuardado: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var serverUrl by remember { mutableStateOf(prefs.serverUrl.ifBlank { "http://10.0.2.2:8081" }) }
    var idMesaText by remember { mutableStateOf(if (prefs.idMesa > 0) prefs.idMesa.toString() else "") }
    var errorMsg by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configuración del Dispositivo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Vote4Tech · Urna",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL del servidor LAN") },
                placeholder = { Text("http://192.168.1.100:8080") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = idMesaText,
                onValueChange = { idMesaText = it },
                label = { Text("ID de Mesa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (errorMsg.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val mesaId = idMesaText.toLongOrNull()
                    if (serverUrl.isBlank() || mesaId == null || mesaId <= 0) {
                        errorMsg = "Ingresa una URL válida y un ID de mesa mayor a 0."
                        return@Button
                    }
                    prefs.serverUrl = serverUrl.trimEnd('/')
                    prefs.idMesa = mesaId
                    prefs.tipoMesa = "URNA"
                    RetrofitClient.init(prefs.serverUrl)
                    onGuardado()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar y continuar")
            }
        }
    }
}
