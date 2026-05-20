package com.vote4tech.urna2.ui.votacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vote4tech.urna2.data.remote.dto.CandidatoDto
import com.vote4tech.urna2.ui.VotacionUiState
import com.vote4tech.urna2.ui.VotacionViewModel

private val Navy = Color(0xFF0d2645)
private val NavyMid = Color(0xFF163a6b)
private val Gold = Color(0xFFC8922A)
private val GoldLight = Color(0xFFE8B04A)
private val Green = Color(0xFF16a34a)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF4F6F9)
private val Grey100 = Color(0xFFE8ECF2)
private val TextColor = Color(0xFF1a2540)
private val TextLight = Color(0xFF4a5570)

@Composable
fun VotacionScreen(
    viewModel: VotacionViewModel,
    onCandidatoSeleccionado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.ListoParaConfirmar) {
            onCandidatoSeleccionado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
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
                    text = "MÓDULO ELECTORAL · URNA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seleccione su",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "Candidato",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Seleccione el candidato de su preferencia.",
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val state = uiState) {
                is VotacionUiState.CandidatosListos -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.candidatos) { candidato ->
                            CandidatoItem(
                                candidato = candidato,
                                onClick = { viewModel.seleccionarCandidato(candidato) }
                            )
                        }
                    }
                }
                is VotacionUiState.Cargando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Navy,
                            strokeWidth = 4.dp
                        )
                    }
                }
                is VotacionUiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = Color(0xFFfee2e2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            state.mensaje,
                            fontSize = 13.sp,
                            color = Color(0xFF991b1b),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun CandidatoItem(candidato: CandidatoDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = Navy,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "N°",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = candidato.numero,
                            fontSize = 18.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = candidato.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
                candidato.nombrePartido?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = TextLight
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}