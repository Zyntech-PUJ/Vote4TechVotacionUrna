package com.vote4tech.urna2.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun LoginRegistradorScreen(
    viewModel: VotacionViewModel,
    onLoginExito: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is VotacionUiState.LoginRegistradorExito) {
            viewModel.limpiarError()
            onLoginExito()
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
                    text = "MÓDULO ELECTORAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Acceso de",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "Registrador",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Para acceder a la configuración, un registrador electoral debe autenticarse.",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Input de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {
                    Text(
                        "Usuario",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Grey100
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        "Contraseña",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Navy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState is VotacionUiState.LoginRegistradorError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Grey100
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mensajes de error
            if (uiState is VotacionUiState.LoginRegistradorError) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = Color(0xFFfee2e2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Credenciales incorrectas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFdc2626)
                        )
                        Text(
                            (uiState as VotacionUiState.LoginRegistradorError).mensaje,
                            fontSize = 12.sp,
                            color = Color(0xFF991b1b)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is VotacionUiState.Cargando) {
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
            } else {
                Button(
                    onClick = { viewModel.loginRegistrador(username, password) },
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Ingresar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}