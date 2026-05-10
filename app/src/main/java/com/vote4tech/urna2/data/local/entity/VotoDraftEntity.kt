package com.vote4tech.urna2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voto_draft")
data class VotoDraftEntity(
    @PrimaryKey val id: String,
    val cedula: String,
    val nombreCiudadano: String,
    val idEleccion: Long = 0L,
    val nombreEleccion: String = "",
    val idMesa: Long,
    val tipoMesa: String = "URNA",
    val tipoSeleccion: String? = null,
    val idSeleccion: Long? = null,
    val nombreSeleccion: String? = null,
    val estado: String = EstadoDraft.IDENTIFICADO.name,
    val creadoEn: Long = System.currentTimeMillis()
)

enum class EstadoDraft {
    IDENTIFICADO, ELECCION_SELECCIONADA, CANDIDATO_SELECCIONADO, CONFIRMADO, ENVIADO
}
