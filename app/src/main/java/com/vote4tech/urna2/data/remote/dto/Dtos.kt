package com.vote4tech.urna2.data.remote.dto

data class CiudadanoDto(
    val idCiudadano: Long,
    val nombre: String,
    val cedula: String,
    val genero: String?,
    val votoObligatorio: Boolean = true,
    val habilitadoDomicilio: Boolean = false
)

data class EleccionDto(
    val idEleccion: Long,
    val nombre: String,
    val tipo: String,
    val listaAbierta: Boolean,
    val estado: String,
    val candidatos: List<CandidatoDto>?
)

data class CandidatoDto(
    val idCandidato: Long,
    val nombre: String,
    val numero: String,
    val fotoUrl: String?,
    val nombrePartido: String?,
    val siglaPartido: String?,
    val logoPartido: String?,
    val idLista: Long
)

data class VotoRequest(
    val cedula: String,
    val idEleccion: Long,
    val idMesa: Long,
    val tipoSeleccion: String,
    val idSeleccion: Long
)

data class VotoResponse(
    val votoId: String,
    val status: String
)

data class ServerInfoDto(
    val version: String?,
    val status: String?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val exito: Boolean,
    val nombre: String?,
    val mensaje: String?
)

data class MesaInfoDto(
    val idMesa: Long,
    val numero: Int,
    val tipo: String,
    val centro: String?
)

data class HotspotConfigDto(
    val ssid: String?,
    val password: String?,
    val canal: Int?,
    val puerto: Int?
)

data class DispositivoRegistroRequest(
    val nombreDispositivo: String,
    val idMesa: Long,
    val tipoMesa: String,
    val centro: String?,
    val ipLocal: String?
)

data class DispositivoEstadoResponse(
    val activo: Boolean
)
