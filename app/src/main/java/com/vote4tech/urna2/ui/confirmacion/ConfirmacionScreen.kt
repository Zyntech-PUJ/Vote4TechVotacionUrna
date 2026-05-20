package com.vote4tech.urna2.ui.confirmacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ConfirmacionScreen(
    viewModel: VotacionViewModel,
    onVotoCompletado: () -> Unit,
    onVotarEnOtraEleccion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        when (val state = uiState) {
            is VotacionUiState.ListoParaConfirmar -> {
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
                            text = "MÓDULO ELECTORAL · CONFIRMACIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confirme su",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = "Voto",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Revise los datos antes de confirmar su voto.",
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Candidato seleccionado:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                letterSpacing = 0.5.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    color = Navy,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = state.nombreCandidato.take(1),
                                            fontSize = 20.sp,
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = state.nombreCandidato,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextColor
                                    )
                                    state.nombrePartido?.let {
                                        Text(
                                            text = it,
                                            fontSize = 12.sp,
                                            color = TextLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.confirmarVoto() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "Confirmar y Votar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.reiniciar(); onVotoCompletado() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, Navy, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            is VotacionUiState.VotoRegistrado -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OffWhite)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        color = Green,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "¡Voto registrado!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Su voto ha sido registrado exitosamente de forma segura y anónima.",
                            fontSize = 13.sp,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.prepararNuevaEleccionMismoCiudadano()
                            onVotarEnOtraEleccion()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Votar en otra elección",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.reiniciar()
                            onVotoCompletado()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, Navy, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Salir",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    }
                }
            }

            is VotacionUiState.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OffWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Navy,
                            strokeWidth = 4.dp
                        )
                        Text(
                            "Registrando voto...",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                }
            }

            is VotacionUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = Color(0xFFfee2e2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Error",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFdc2626)
                            )
                            Text(
                                state.mensaje,
                                fontSize = 13.sp,
                                color = Color(0xFF991b1b),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.confirmarVoto() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Reintentar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.limpiarError() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, Navy, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Volver",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    }
                }
            }

            else -> {}
        }
    }
}