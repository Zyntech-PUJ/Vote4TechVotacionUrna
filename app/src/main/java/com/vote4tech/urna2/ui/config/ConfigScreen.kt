package com.vote4tech.urna2.ui.config

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

    var ssid by remember { mutableStateOf(prefs.hotspotSsid) }
    var wifiPassword by remember { mutableStateOf(prefs.hotspotPassword) }
    var serverUrl by remember { mutableStateOf(prefs.serverUrl.ifBlank { "http://10.0.2.2:8081" }) }
    var wifiPasswordVisible by remember { mutableStateOf(false) }

    var mesas by remember { mutableStateOf<List<MesaInfoDto>>(emptyList()) }
    var mesaSeleccionada by remember { mutableStateOf<MesaInfoDto?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mesas) {
        if (mesas.isNotEmpty() && prefs.idMesa > 0L && mesaSeleccionada == null) {
            mesaSeleccionada = mesas.find { it.idMesa == prefs.idMesa }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Configuración de Mesa",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // ── Hotspot ──────────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Red hotspot del servidor",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ingresa los datos del hotspot configurado en el servidor local del centro de votación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = ssid,
                    onValueChange = {
                        ssid = it
                        error = null
                        mesas = emptyList()
                        mesaSeleccionada = null
                    },
                    label = { Text("SSID del hotspot") },
                    placeholder = { Text("Vote4Tech_Urna_01") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it; error = null },
                    label = { Text("Contraseña del hotspot") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (wifiPasswordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { wifiPasswordVisible = !wifiPasswordVisible },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(if (wifiPasswordVisible) "Ocultar" else "Mostrar")
                        }
                    }
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
                    placeholder = { Text("http://10.0.2.2:8081") },
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
                            // Intento de conexión WiFi (best effort)
                            if (ssid.isNotBlank() && wifiPassword.isNotBlank()) {
                                suggestWifi(context, ssid.trim(), wifiPassword)
                                delay(1500)
                            }
                            // Verificar conexión al servidor con reintentos
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
                                Toast.makeText(
                                    context,
                                    "Conectado al servidor correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                try {
                                    val mesasResp = RetrofitClient.api.getMesas()
                                    if (mesasResp.isSuccessful && mesasResp.body() != null) {
                                        mesas = mesasResp.body()!!
                                        if (mesas.isEmpty()) {
                                            error = "El servidor no tiene mesas cargadas. Ejecuta la sincronización primero."
                                        }
                                    } else {
                                        error = "Error al cargar mesas: ${mesasResp.code()}"
                                    }
                                } catch (e: Exception) {
                                    error = "Error al cargar mesas: ${e.message}"
                                }
                            } else {
                                error = "No se pudo conectar al servidor. Verifica que el dispositivo esté en la red del hotspot y que la URL sea correcta."
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

        // ── Mensajes ─────────────────────────────────────────────────────────
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ── Lista de mesas ───────────────────────────────────────────────────
        if (mesas.isNotEmpty()) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
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
                                        "Mesa ${mesa.numero} — ${mesa.tipo}",
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
            Spacer(Modifier.weight(1f))
        }

        // ── Botón guardar ────────────────────────────────────────────────────
        Button(
            onClick = {
                when {
                    ssid.isBlank() -> error = "Ingresa el SSID del hotspot"
                    serverUrl.isBlank() -> error = "Ingresa la URL del servidor"
                    mesaSeleccionada == null -> error = "Selecciona una mesa de la lista"
                    else -> {
                        prefs.hotspotSsid = ssid.trim()
                        prefs.hotspotPassword = wifiPassword
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
            Text("Guardar configuración")
        }
    }
}

/**
 * Intenta agregar una sugerencia/configuración de red WiFi al sistema (best effort).
 * En Android 10+ usa WifiNetworkSuggestion; en versiones anteriores usa WifiConfiguration.
 */
@Suppress("DEPRECATION")
private fun suggestWifi(context: Context, ssid: String, password: String) {
    try {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addNetworkSuggestion(wifiManager, ssid, password)
        } else {
            val wifiConfig = android.net.wifi.WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$password\""
            }
            val netId = wifiManager.addNetwork(wifiConfig)
            if (netId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
            }
        }
    } catch (_: Exception) {
        // Best effort — no interrumpir el flujo si falla la operación WiFi
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun addNetworkSuggestion(wifiManager: WifiManager, ssid: String, password: String) {
    val suggestion = WifiNetworkSuggestion.Builder()
        .setSsid(ssid)
        .setWpa2Passphrase(password)
        .build()
    wifiManager.removeNetworkSuggestions(emptyList())
    wifiManager.addNetworkSuggestions(listOf(suggestion))
}

