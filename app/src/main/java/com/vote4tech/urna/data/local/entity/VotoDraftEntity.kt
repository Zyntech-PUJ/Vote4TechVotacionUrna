package com.vote4tech.urna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Borrador de voto persistido en Room.
 * Sobrevive cierres inesperados del dispositivo.
 * Estado avanza a través del flujo de votación; solo cuando
 * el servidor confirma el voto se marca ENVIADO.
 */
@Entity(tableName = "voto_draft")
data class VotoDraftEntity(
    @PrimaryKey val id: String,
    val cedula: String,
    val nombreCiudadano: String,
    val idEleccion: Long,
    val nombreEleccion: String,
    val idMesa: Long,
    val tipoMesa: String = "URNA",
    val tipoSeleccion: String? = null,   // "CANDIDATO" | "LISTA"
    val idSeleccion: Long? = null,
    val nombreSeleccion: String? = null,
    val estado: String = EstadoDraft.IDENTIFICADO.name,
    val creadoEn: Long = System.currentTimeMillis()
)

enum class EstadoDraft {
    IDENTIFICADO,
    ELECCION_SELECCIONADA,
    CANDIDATO_SELECCIONADO,
    CONFIRMADO,
    ENVIADO
}
