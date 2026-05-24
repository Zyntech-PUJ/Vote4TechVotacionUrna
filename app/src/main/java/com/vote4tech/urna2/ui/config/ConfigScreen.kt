package com.vote4tech.urna2.ui.config

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.data.remote.RetrofitClient
import com.vote4tech.urna2.data.remote.dto.MesaInfoDto
import com.vote4tech.urna2.util.PrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(
    prefs: PrefsManager,
    onConfigGuardada: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var serverUrl by remember { mutableStateOf(prefs.serverUrl.ifBlank { "http://192.168.43.21:8081" }) }

    var mesas by remember { mutableStateOf<List<MesaInfoDto>>(emptyList()) }
    var mesaSeleccionada by remember { mutableStateOf<MesaInfoDto?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mesas) {
        if (mesas.isNotEmpty() && prefs.idMesa > 0L && mesaSeleccionada == null) {
            mesaSeleccionada = mesas.find { it.idMesa == prefs.idMesa }
        }
    }

    // Layout horizontal: izquierda = URL + botÃ³n, derecha = lista de mesas
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // â”€â”€ Panel izquierdo: URL del servidor â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "ConfiguraciÃ³n de Mesa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "URL del servidor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ingresa la direcciÃ³n IP del portÃ¡til servidor en la red local.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            error = null
                            mesas = emptyList()
                            mesaSeleccionada = null
                        },
                        label = { Text("URL del servidor") },
                        placeholder = { Text("http://192.168.43.21:8081") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    Button(
                        onClick = {
                            error = null
                            cargando = true
                            mesas = emptyList()
                            mesaSeleccionada = null
                            scope.launch {
                                var intentos = 0
                                var conectado = false
                                while (intentos < 3 && !conectado) {
                                    try {
                                        RetrofitClient.init(serverUrl.trim())
                                        val pingResp = RetrofitClient.api.ping()
                                        if (pingResp.isSuccessful) conectado = true
                                    } catch (_: Exception) {}
                                    if (!conectado) {
                                        intentos++
                                        if (intentos < 3) delay(1500)
                                    }
                                }
                                if (conectado) {
                                    Toast.makeText(context, "Conectado al servidor", Toast.LENGTH_SHORT).show()
                                    try {
                                        val mesasResp = RetrofitClient.api.getMesas()
                                        if (mesasResp.isSuccessful && mesasResp.body() != null) {
                                            mesas = mesasResp.body()!!
                                            if (mesas.isEmpty()) {
                                                error = "El servidor no tiene mesas cargadas. Ejecuta la sincronizaciÃ³n primero."
                                            }
                                        } else {
                                            error = "Error al cargar mesas: ${mesasResp.code()}"
                                        }
                                    } catch (e: Exception) {
                                        error = "Error al cargar mesas: ${e.message}"
                                    }
                                } else {
                                    error = "No se pudo conectar. Verifica que la URL sea correcta y estÃ©s en la misma red."
                                }
                                cargando = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = serverUrl.isNotBlank() && !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (cargando) "Conectando..." else "Conectar y cargar mesas")
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        // â”€â”€ Panel derecho: selecciÃ³n de mesa + guardar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "SelecciÃ³n de Mesa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (mesas.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Selecciona tu mesa:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(mesas) { mesa ->
                                val seleccionada = mesaSeleccionada?.idMesa == mesa.idMesa
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { mesaSeleccionada = mesa; error = null },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (seleccionada)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            "Mesa ${mesa.numero} â€” ${mesa.tipo}",
                                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (!mesa.centro.isNullOrBlank()) {
                                            Text(
                                                mesa.centro,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "ID: ${mesa.idMesa}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Conecta al servidor para ver\nlas mesas disponibles",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    when {
                        serverUrl.isBlank() -> error = "Ingresa la URL del servidor"
                        mesaSeleccionada == null -> error = "Selecciona una mesa de la lista"
                        else -> {
                            prefs.serverUrl = serverUrl.trim()
                            prefs.idMesa = mesaSeleccionada!!.idMesa
                            prefs.tipoMesa = mesaSeleccionada!!.tipo
                            RetrofitClient.init(serverUrl.trim())
                            onConfigGuardada()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mesaSeleccionada != null
            ) {
                Text("Guardar configuraciÃ³n")
            }
        }
    }
}

