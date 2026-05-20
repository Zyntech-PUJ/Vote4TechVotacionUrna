package com.vote4tech.urna2.ui.config

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vote4tech.urna2.data.remote.RetrofitClient
import com.vote4tech.urna2.data.remote.dto.MesaInfoDto
import com.vote4tech.urna2.util.PrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Navy = Color(0xFF0d2645)
private val NavyMid = Color(0xFF163a6b)
private val Gold = Color(0xFFC8922A)
private val GoldLight = Color(0xFFE8B04A)
private val Green = Color(0xFF16a34a)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF4F6F9)
private val Grey100 = Color(0xFFE8ECF2)
private val Grey300 = Color(0xFFD1D5DB)
private val TextColor = Color(0xFF1a2540)
private val TextLight = Color(0xFF4a5570)

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
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // Header con padding para notch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Navy, NavyMid)
                    )
                )
                .padding(top = 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(
                    text = "MÓDULO ELECTORAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configuración",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "de Mesa",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ingresa los datos de tu mesa electoral.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Hotspot ──────────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Red hotspot del servidor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Ingresa los datos del hotspot configurado en el servidor local del centro de votación.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = ssid,
                        onValueChange = {
                            ssid = it
                            error = null
                            mesas = emptyList()
                            mesaSeleccionada = null
                        },
                        label = { Text("SSID del hotspot", fontSize = 13.sp) },
                        placeholder = { Text("Vote4Tech_Urna_01", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = Grey100
                        )
                    )

                    OutlinedTextField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it; error = null },
                        label = { Text("Contraseña del hotspot", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = if (wifiPasswordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(
                                onClick = { wifiPasswordVisible = !wifiPasswordVisible },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    if (wifiPasswordVisible) "Ocultar" else "Mostrar",
                                    fontSize = 12.sp,
                                    color = Navy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = Grey100
                        )
                    )

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            error = null
                            mesas = emptyList()
                            mesaSeleccionada = null
                        },
                        label = { Text("URL del servidor", fontSize = 13.sp) },
                        placeholder = { Text("http://10.0.2.2:8081", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy,
                            unfocusedBorderColor = Grey100
                        )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(10.dp),
                        enabled = serverUrl.isNotBlank() && !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = White
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (cargando) "Conectando..." else "Conectar y cargar mesas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }

            // ── Mensajes ─────────────────────────────────────────────────────────
            error?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = Color(0xFFfee2e2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        it,
                        color = Color(0xFF991b1b),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // ── Lista de mesas ───────────────────────────────────────────────────
            if (mesas.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Selecciona tu mesa:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColor
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mesas) { mesa ->
                                val seleccionada = mesaSeleccionada?.idMesa == mesa.idMesa
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { mesaSeleccionada = mesa; error = null },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (seleccionada)
                                            Color(0xFFe0f2fe)
                                        else
                                            White
                                    ),
                                    border = CardDefaults.outlinedCardBorder(
                                        enabled = seleccionada
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Mesa ${mesa.numero} — ${mesa.tipo}",
                                                    fontSize = 13.sp,
                                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                                                    color = TextColor
                                                )
                                                if (!mesa.centro.isNullOrBlank()) {
                                                    Text(
                                                        mesa.centro,
                                                        fontSize = 11.sp,
                                                        color = TextLight
                                                    )
                                                }
                                                Text(
                                                    "ID: ${mesa.idMesa}",
                                                    fontSize = 10.sp,
                                                    color = TextLight
                                                )
                                            }
                                            if (seleccionada) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Green,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(10.dp),
                enabled = mesaSeleccionada != null
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Guardar configuración",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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