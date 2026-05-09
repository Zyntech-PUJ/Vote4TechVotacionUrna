package com.vote4tech.urna.data.remote.dto

data class CiudadanoDto(
    val idCiudadano: Long,
    val nombre: String,
    val cedula: String,
    val genero: String?,
    val votoObligatorio: Boolean
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
    val fotoUrl: String,
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
    val version: String,
    val status: String,
    val timestamp: Long
)
