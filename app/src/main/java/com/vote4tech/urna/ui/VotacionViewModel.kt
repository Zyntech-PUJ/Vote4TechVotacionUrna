package com.vote4tech.urna.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vote4tech.urna.data.local.dao.VotoDraftDao
import com.vote4tech.urna.data.local.entity.EstadoDraft
import com.vote4tech.urna.data.local.entity.VotoDraftEntity
import com.vote4tech.urna.data.remote.RetrofitClient
import com.vote4tech.urna.data.remote.dto.CandidatoDto
import com.vote4tech.urna.data.remote.dto.EleccionDto
import com.vote4tech.urna.data.remote.dto.VotoRequest
import com.vote4tech.urna.util.PrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VotacionViewModel(
    private val prefs: PrefsManager,
    private val draftDao: VotoDraftDao
) : ViewModel() {

    // ── Estado de UI ──────────────────────────────────────────
    private val _uiState = MutableStateFlow<VotacionUiState>(VotacionUiState.Idle)
    val uiState: StateFlow<VotacionUiState> = _uiState.asStateFlow()

    // ── Datos en memoria durante la sesión ────────────────────
    private var currentDraft: VotoDraftEntity? = null
    private val _elecciones = MutableStateFlow<List<EleccionDto>>(emptyList())
    val elecciones: StateFlow<List<EleccionDto>> = _elecciones.asStateFlow()
    private val _candidatos = MutableStateFlow<List<CandidatoDto>>(emptyList())
    val candidatos: StateFlow<List<CandidatoDto>> = _candidatos.asStateFlow()

    // ── Al iniciar: revisar si hay un borrador pendiente ─────
    fun verificarDraftPendiente(onHayDraft: (EstadoDraft) -> Unit) {
        viewModelScope.launch {
            val pendiente = draftDao.obtenerPendiente()
            if (pendiente != null) {
                currentDraft = pendiente
                onHayDraft(EstadoDraft.valueOf(pendiente.estado))
            }
        }
    }

    // ── Paso 1: Identificar ciudadano ─────────────────────────
    fun identificarCiudadano(cedula: String) {
        _uiState.value = VotacionUiState.Cargando
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getCiudadano(cedula)
                if (resp.isSuccessful && resp.body() != null) {
                    val ciudadano = resp.body()!!
                    // Crear draft en Room (fault tolerance desde aquí)
                    val draft = VotoDraftEntity(
                        id = UUID.randomUUID().toString(),
                        cedula = ciudadano.cedula,
                        nombreCiudadano = ciudadano.nombre,
                        idEleccion = 0L,
                        nombreEleccion = "",
                        idMesa = prefs.idMesa,
                        tipoMesa = prefs.tipoMesa,
                        estado = EstadoDraft.IDENTIFICADO.name
                    )
                    draftDao.insertar(draft)
                    currentDraft = draft
                    _uiState.value = VotacionUiState.CiudadanoIdentificado(ciudadano.nombre, ciudadano.cedula)
                } else {
                    _uiState.value = VotacionUiState.Error("Cédula no encontrada en el sistema.")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Sin conexión al servidor LAN. Verifica la red.")
            }
        }
    }

    // ── Paso 2: Cargar elecciones activas ─────────────────────
    fun cargarElecciones() {
        _uiState.value = VotacionUiState.Cargando
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getEleccionesActivas()
                if (resp.isSuccessful) {
                    _elecciones.value = resp.body() ?: emptyList()
                    _uiState.value = VotacionUiState.EleccionesListas
                } else {
                    _uiState.value = VotacionUiState.Error("No se pudieron cargar las elecciones.")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Error de red: ${e.message}")
            }
        }
    }

    // ── Paso 3: Seleccionar elección ──────────────────────────
    fun seleccionarEleccion(eleccion: EleccionDto) {
        viewModelScope.launch {
            currentDraft = currentDraft?.copy(
                idEleccion = eleccion.idEleccion,
                nombreEleccion = eleccion.nombre,
                estado = EstadoDraft.ELECCION_SELECCIONADA.name
            )
            currentDraft?.let { draftDao.actualizar(it) }
            cargarCandidatos(eleccion.idEleccion)
        }
    }

    // ── Paso 4: Cargar candidatos de la elección ──────────────
    private fun cargarCandidatos(idEleccion: Long) {
        _uiState.value = VotacionUiState.Cargando
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getCandidatos(idEleccion)
                if (resp.isSuccessful) {
                    _candidatos.value = resp.body() ?: emptyList()
                    _uiState.value = VotacionUiState.CandidatosListos
                } else {
                    _uiState.value = VotacionUiState.Error("No se pudieron cargar los candidatos.")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Error de red: ${e.message}")
            }
        }
    }

    // ── Paso 5: Seleccionar candidato/lista ───────────────────
    fun seleccionarCandidato(candidato: CandidatoDto) {
        viewModelScope.launch {
            currentDraft = currentDraft?.copy(
                tipoSeleccion = "CANDIDATO",
                idSeleccion = candidato.idCandidato,
                nombreSeleccion = candidato.nombre,
                estado = EstadoDraft.CANDIDATO_SELECCIONADO.name
            )
            currentDraft?.let { draftDao.actualizar(it) }
            _uiState.value = VotacionUiState.ListoParaConfirmar(
                nombreCandidato = candidato.nombre,
                nombrePartido = candidato.nombrePartido ?: ""
            )
        }
    }

    // ── Paso 6: Confirmar y enviar ────────────────────────────
    fun confirmarVoto() {
        val draft = currentDraft ?: return
        _uiState.value = VotacionUiState.Cargando
        viewModelScope.launch {
            try {
                // Marcar CONFIRMADO antes de enviar (fault tolerance)
                draftDao.actualizarEstado(draft.id, EstadoDraft.CONFIRMADO.name)

                val request = VotoRequest(
                    cedula = draft.cedula,
                    idEleccion = draft.idEleccion,
                    idMesa = draft.idMesa,
                    tipoSeleccion = draft.tipoSeleccion ?: "CANDIDATO",
                    idSeleccion = draft.idSeleccion ?: 0L
                )
                val resp = RetrofitClient.api.votar(request)
                if (resp.isSuccessful) {
                    draftDao.actualizarEstado(draft.id, EstadoDraft.ENVIADO.name)
                    draftDao.limpiarEnviados()
                    currentDraft = null
                    _uiState.value = VotacionUiState.VotoRegistrado
                } else {
                    _uiState.value = VotacionUiState.Error(
                        "Error al registrar el voto: ${resp.code()}. El borrador está guardado."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error(
                    "Sin conexión. El borrador está guardado y se enviará al restaurar la red."
                )
            }
        }
    }

    // ── Reiniciar para el siguiente votante ───────────────────
    fun reiniciar() {
        currentDraft = null
        _elecciones.value = emptyList()
        _candidatos.value = emptyList()
        _uiState.value = VotacionUiState.Idle
    }

    fun limpiarError() { _uiState.value = VotacionUiState.Idle }
}

// ── Estados de la UI ──────────────────────────────────────────
sealed class VotacionUiState {
    data object Idle : VotacionUiState()
    data object Cargando : VotacionUiState()
    data object EleccionesListas : VotacionUiState()
    data object CandidatosListos : VotacionUiState()
    data object VotoRegistrado : VotacionUiState()
    data class CiudadanoIdentificado(val nombre: String, val cedula: String) : VotacionUiState()
    data class ListoParaConfirmar(val nombreCandidato: String, val nombrePartido: String) : VotacionUiState()
    data class Error(val mensaje: String) : VotacionUiState()
}
