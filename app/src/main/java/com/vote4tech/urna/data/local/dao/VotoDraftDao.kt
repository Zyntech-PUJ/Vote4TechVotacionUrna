package com.vote4tech.urna.data.local.dao

import androidx.room.*
import com.vote4tech.urna.data.local.entity.VotoDraftEntity

@Dao
interface VotoDraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(draft: VotoDraftEntity)

    @Update
    suspend fun actualizar(draft: VotoDraftEntity)

    /** Devuelve el primer borrador pendiente de enviar (para reanudar tras un fallo). */
    @Query("SELECT * FROM voto_draft WHERE estado != 'ENVIADO' LIMIT 1")
    suspend fun obtenerPendiente(): VotoDraftEntity?

    @Query("UPDATE voto_draft SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String)

    @Query("UPDATE voto_draft SET tipoSeleccion = :tipo, idSeleccion = :idSeleccion, nombreSeleccion = :nombre, estado = 'CANDIDATO_SELECCIONADO' WHERE id = :id")
    suspend fun registrarSeleccion(id: String, tipo: String, idSeleccion: Long, nombre: String)

    @Query("DELETE FROM voto_draft WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("DELETE FROM voto_draft WHERE estado = 'ENVIADO'")
    suspend fun limpiarEnviados()
}
