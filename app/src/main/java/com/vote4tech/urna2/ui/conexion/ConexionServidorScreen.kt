package com.vote4tech.urna2.ui.conexion

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.vote4tech.urna2.ui.VotacionViewModel
import com.vote4tech.urna2.util.PrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConexionServidorScreen(
    viewModel: VotacionViewModel,
    prefs: PrefsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val serverOnline by viewModel.serverOnline.collectAsState()

    // Estado de verificación
    var verificando by remember { mutableStateOf(false) }

    // Estado de cambio de URL
    var nuevaUrl by remember { mutableStateOf(prefs.serverUrl) }
    var mesas by remember { mutableStateOf<List<MesaInfoDto>>(emptyList()) }
    var mesaSeleccionada by remember { mutableStateOf<MesaInfoDto?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var errorUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mesas) {
        if (mesas.isNotEmpty() && prefs.idMesa > 0L && mesaSeleccionada == null) {
            mesaSeleccionada = mesas.find { it.idMesa == prefs.idMesa }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Panel izquierdo ───────────────────────────────────────────────────
        Column(
            modifier = Modifier.width(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Conexión al Servidor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Verifica el estado de la conexión con el servidor local de urna y cambia la URL si es necesario.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver")
            }
        }

        // ── Panel derecho ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card: info del servidor actual + verificar
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Servidor actual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    InfoRow("URL", prefs.serverUrl.ifBlank { "No configurada" })
                    InfoRow("Mesa", "ID ${prefs.idMesa} · ${prefs.tipoMesa}")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    val (statusColor, statusText, statusEmoji) = when (serverOnline) {
                        true  -> Triple(MaterialTheme.colorScheme.primary, "En línea", "🟢")
                        false -> Triple(MaterialTheme.colorScheme.error, "Sin conexión", "🔴")
                        null  -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, "Sin verificar", "⏳")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(statusEmoji, style = MaterialTheme.typography.titleMedium)
                        Text(statusText, style = MaterialTheme.typography.bodyMedium, color = statusColor, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            verificando = true
                            scope.launch {
                                viewModel.verificarAhora()
                                delay(2500)
                                verificando = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !verificando && prefs.serverUrl.isNotBlank()
                    ) {
                        if (verificando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (verificando) "Verificando..." else "Verificar conexión")
                    }
                }
            }

            // Card: cambiar URL
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Cambiar servidor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Ingresa la IP del portátil servidor en la red local y carga las mesas disponibles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nuevaUrl,
                        onValueChange = {
                            nuevaUrl = it
                            errorUrl = null
                            mesas = emptyList()
                            mesaSeleccionada = null
                        },
                        label = { Text("URL del servidor") },
                        placeholder = { Text("http://192.168.43.21:8081") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        isError = errorUrl != null
                    )
                    errorUrl?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            errorUrl = null
                            cargando = true
                            mesas = emptyList()
                            mesaSeleccionada = null
                            scope.launch {
                                var intentos = 0
                                var conectado = false
                                while (intentos < 3 && !conectado) {
                                    try {
                                        RetrofitClient.init(nuevaUrl.trim())
                                        if (RetrofitClient.api.ping().isSuccessful) conectado = true
                                    } catch (_: Exception) {}
                                    if (!conectado) { intentos++; if (intentos < 3) delay(1500) }
                                }
                                if (conectado) {
                                    Toast.makeText(context, "Conectado", Toast.LENGTH_SHORT).show()
                                    try {
                                        val resp = RetrofitClient.api.getMesas()
                                        if (resp.isSuccessful && resp.body() != null) {
                                            mesas = resp.body()!!
                                            if (mesas.isEmpty()) errorUrl = "El servidor no tiene mesas cargadas. Ejecuta la sincronización primero."
                                        } else errorUrl = "Error al cargar mesas: ${resp.code()}"
                                    } catch (e: Exception) { errorUrl = "Error: ${e.message}" }
                                } else {
                                    errorUrl = "No se pudo conectar. Verifica la URL y que estés en la misma red."
                                }
                                cargando = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = nuevaUrl.isNotBlank() && !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (cargando) "Conectando..." else "Conectar y cargar mesas")
                    }

                    if (mesas.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Selecciona tu mesa:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(mesas) { mesa ->
                                val seleccionada = mesaSeleccionada?.idMesa == mesa.idMesa
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { mesaSeleccionada = mesa; errorUrl = null },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (seleccionada)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            "Mesa ${mesa.numero} – ${mesa.tipo}",
                                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (!mesa.centro.isNullOrBlank()) {
                                            Text(mesa.centro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("ID: ${mesa.idMesa}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                when {
                                    nuevaUrl.isBlank() -> errorUrl = "Ingresa la URL del servidor"
                                    mesaSeleccionada == null -> errorUrl = "Selecciona una mesa"
                                    else -> {
                                        prefs.serverUrl = nuevaUrl.trim()
                                        prefs.idMesa = mesaSeleccionada!!.idMesa
                                        prefs.tipoMesa = mesaSeleccionada!!.tipo
                                        prefs.centro = mesaSeleccionada!!.centro ?: ""
                                        RetrofitClient.init(nuevaUrl.trim())
                                        viewModel.registrarDispositivo()
                                        Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = mesaSeleccionada != null
                        ) {
                            Text("Guardar configuración")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
