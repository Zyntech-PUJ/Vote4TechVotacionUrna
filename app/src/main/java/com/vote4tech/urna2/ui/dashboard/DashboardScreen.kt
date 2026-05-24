package com.vote4tech.urna2.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

@Composable
fun DashboardScreen(
    viewModel: VotacionViewModel,
    onNavConexion: () -> Unit,
    onNavInstrucciones: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val nombreUsuario = remember(uiState) {
        (uiState as? VotacionUiState.LoginRegistradorExito)?.nombre ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Info panel ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.width(220.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Panel de Administración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            if (nombreUsuario.isNotBlank()) {
                Text("👤 $nombreUsuario", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("← Cerrar sesión")
            }
        }

        // ── Botones del dashboard ─────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashCard(
                    emoji = "🔌",
                    title = "Conexión al Servidor",
                    subtitle = "Ver estado y cambiar URL del servidor",
                    onClick = onNavConexion,
                    modifier = Modifier.weight(1f)
                )
                DashCard(
                    emoji = "📋",
                    title = "Instrucciones",
                    subtitle = "Guía de configuración de red y urna",
                    onClick = onNavInstrucciones,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashCard(
                    emoji = "🗳️",
                    title = "Mesa: ${viewModel.tipoMesa}",
                    subtitle = buildString {
                        append("ID ${viewModel.idMesaActual}")
                        if (viewModel.centroActual.isNotBlank()) append(" · ${viewModel.centroActual}")
                    },
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
