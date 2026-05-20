package com.vote4tech.urna2.ui.identificacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
private val Grey300 = Color(0xFFD1D5DB)
private val TextColor = Color(0xFF1a2540)
private val TextLight = Color(0xFF4a5570)
private val Red = Color(0xFFdc2626)

@Composable
fun IdentificacionScreen(
    viewModel: VotacionViewModel,
    onCiudadanoIdentificado: () -> Unit,
    onNavConfig: () -> Unit,
    onNavLoginRegistrador: () -> Unit,
    onConfigClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cedula by remember { mutableStateOf("") }

    // Show AlertDialog when citizen is domicilio
    if (uiState is VotacionUiState.CiudadanoDomicilio) {
        val nombre = (uiState as VotacionUiState.CiudadanoDomicilio).nombre
        AlertDialog(
            onDismissRequest = { viewModel.limpiarError(); cedula = "" },
            title = {
                Text(
                    "Acceso Restringido",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
            },
            text = {
                Text(
                    "El ciudadano \"$nombre\" está habilitado para voto en domicilio.\n\n" +
                    "No puede votar en urna. Por favor contáctese con la Registraduría Nacional del Estado Civil para más información.",
                    fontSize = 14.sp,
                    color = TextLight
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarError(); cedula = "" }) {
                    Text("Entendido", color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VotacionUiState.CiudadanoIdentificado -> {
                onCiudadanoIdentificado()
                viewModel.cargarElecciones()
            }
            is VotacionUiState.ConectadoAlServidor -> {
                viewModel.limpiarError()
                onNavLoginRegistrador()
            }
            is VotacionUiState.Error -> {
                if (state.mensaje == "sin_conexion") {
                    viewModel.limpiarError()
                    onNavConfig()
                }
            }
            else -> {}
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Identificación",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "del Votante",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingrese su número de cédula para verificar su identidad.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Input de cédula
            OutlinedTextField(
                value = cedula,
                onValueChange = {
                    cedula = it.filter { c -> c.isDigit() }
                    if (uiState is VotacionUiState.Error) viewModel.limpiarError()
                },
                label = {
                    Text(
                        "Número de cédula",
                        fontSize = 14.sp,
                        color = TextLight
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { if (cedula.length >= 6) viewModel.identificarCiudadano(cedula) }
                ),
                isError = uiState is VotacionUiState.Error,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Grey100,
                    focusedTextColor = TextColor
                )
            )

            // Mensajes de estado
            when (val state = uiState) {
                is VotacionUiState.Cargando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                    if (state.mensaje != "sin_conexion") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            color = Color(0xFFfee2e2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Error",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Red
                                )
                                Text(
                                    state.mensaje,
                                    fontSize = 12.sp,
                                    color = Color(0xFF991b1b),
                                    lineHeight = 16.sp
                                )
                                TextButton(
                                    onClick = { viewModel.limpiarError(); cedula = "" }
                                ) {
                                    Text(
                                        "Intentar de nuevo",
                                        color = Navy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                is VotacionUiState.DraftPendiente -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Votación en progreso",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                            Text(
                                state.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                            Text(
                                "Elección: ${state.eleccion}",
                                fontSize = 13.sp,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onCiudadanoIdentificado,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Continuar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones
            Button(
                onClick = { viewModel.identificarCiudadano(cedula) },
                enabled = cedula.length >= 6 && uiState !is VotacionUiState.Cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState is VotacionUiState.Cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    "Verificar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onConfigClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(2.dp, Navy, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Navy,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Configuración",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}