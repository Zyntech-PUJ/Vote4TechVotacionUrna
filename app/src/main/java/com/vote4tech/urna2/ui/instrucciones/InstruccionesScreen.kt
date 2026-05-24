package com.vote4tech.urna2.ui.instrucciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InstruccionesScreen(
    onBack: () -> Unit
) {
    val pasos = listOf(
        Triple("📱", "Dispositivo para hotspot",
            "Toma un teléfono Android independiente (NO la tablet de urna). Este será el punto de acceso WiFi."),
        Triple("⚙️", "Abrir configuración de hotspot",
            "Ve a Ajustes → Conexiones → Punto de acceso móvil (varía según la marca: puede aparecer como \"Zona WiFi\", \"Hotspot personal\" o similar)."),
        Triple("✅", "Activar el hotspot",
            "Activa el interruptor de Punto de acceso móvil / Hotspot."),
        Triple("📝", "Configurar nombre y contraseña",
            "Establece el nombre de la red (SSID): Vote4Tech_Urna\nContraseña: voto2025\n\nEs importante usar siempre estos mismos datos para que el sistema funcione correctamente."),
        Triple("💻", "Conectar el portátil servidor",
            "En el portátil donde corre el servidor Docker, conéctate al WiFi \"Vote4Tech_Urna\" con la contraseña configurada."),
        Triple("🖥️", "Levantar el servidor Docker",
            "En el portátil, abre PowerShell en la carpeta Vote4TechServidorLocalUrna y ejecuta:\n\ndocker compose up -d\n\nEspera a que los contenedores estén en estado 'healthy'."),
        Triple("📡", "Conectar la tablet de urna",
            "En la tablet de urna, ve a Ajustes → WiFi y conecta la red \"Vote4Tech_Urna\" con la contraseña voto2025."),
        Triple("🔧", "Configurar la app de urna",
            "En la app de urna, toca el botón ⚙️ Configuración → ingresa tus credenciales de registrador → en la pantalla de configuración escribe la URL del servidor:\n\nhttp://[IP del portátil]:8081\n\nLa IP del portátil en la red hotspot suele ser 192.168.43.x (puedes verla en la configuración del hotspot o ejecutando ipconfig en el portátil).\n\nLuego toca \"Conectar y cargar mesas\", selecciona la mesa y guarda.")
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Título + botón volver ─────────────────────────────────────────────
        Column(
            modifier = Modifier.width(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Instrucciones de Configuración",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sigue estos pasos para preparar la red y la urna antes de la jornada de votación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver")
            }
        }

        // ── Lista de pasos ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pasos.forEachIndexed { idx, (emoji, titulo, descripcion) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Número de paso
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${idx + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("$emoji $titulo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
