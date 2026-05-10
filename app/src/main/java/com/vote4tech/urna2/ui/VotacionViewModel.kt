package com.vote4tech.urna2.ui

import com.vote4tech.urna2.data.local.dao.VotoDraftDao
import com.vote4tech.urna2.data.local.entity.EstadoDraft
import com.vote4tech.urna2.data.local.entity.VotoDraftEntity
import com.vote4tech.urna2.data.remote.RetrofitClient
import com.vote4tech.urna2.data.remote.dto.CandidatoDto
import com.vote4tech.urna2.data.remote.dto.EleccionDto
import com.vote4tech.urna2.data.remote.dto.VotoRequest
import com.vote4tech.urna2.util.PrefsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class VotacionUiState {
    object Idle : VotacionUiState()
    object Cargando : VotacionUiState()
    data class CiudadanoIdentificado(val nombre: String) : VotacionUiState()
    data class EleccionesListas(val elecciones: List<EleccionDto>) : VotacionUiState()
    data class CandidatosListos(val candidatos: List<CandidatoDto>) : VotacionUiState()
    data class ListoParaConfirmar(val draftId: String, val nombreCandidato: String, val nombrePartido: String?) : VotacionUiState()
    object VotoRegistrado : VotacionUiState()
    data class Error(val mensaje: String) : VotacionUiState()
    data class DraftPendiente(val nombre: String, val eleccion: String) : VotacionUiState()
    data class YaVoto(val nombreEleccion: String) : VotacionUiState()
}

class VotacionViewModel(
    private val prefs: PrefsManager,
    private val votoDraftDao: VotoDraftDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<VotacionUiState>(VotacionUiState.Idle)
    val uiState: StateFlow<VotacionUiState> = _uiState

    private var draftActual: VotoDraftEntity? = null
    private var eleccionesCache: List<EleccionDto> = emptyList()

    fun verificarDraftPendiente() {
        viewModelScope.launch {
            val draft = votoDraftDao.obtenerPendiente()
            if (draft != null) {
                draftActual = draft
                _uiState.value = VotacionUiState.DraftPendiente(draft.nombreCiudadano, draft.nombreEleccion)
            }
        }
    }

    fun identificarCiudadano(cedula: String) {
        viewModelScope.launch {
            _uiState.value = VotacionUiState.Cargando
            try {
                val response = RetrofitClient.api.getCiudadano(cedula)
                if (response.isSuccessful) {
                    val ciudadano = response.body()!!
                    val draft = VotoDraftEntity(
                        id = UUID.randomUUID().toString(),
                        cedula = ciudadano.cedula,
                        nombreCiudadano = ciudadano.nombre,
                        idMesa = prefs.idMesa
                    )
                    votoDraftDao.limpiarEnviados()
                    votoDraftDao.insertar(draft)
                    draftActual = draft
                    _uiState.value = VotacionUiState.CiudadanoIdentificado(ciudadano.nombre)
                } else if (response.code() == 404) {
                    _uiState.value = VotacionUiState.Error("Ciudadano no encontrado")
                } else {
                    _uiState.value = VotacionUiState.Error("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun cargarElecciones() {
        viewModelScope.launch {
            _uiState.value = VotacionUiState.Cargando
            try {
                val response = RetrofitClient.api.getEleccionesActivas()
                if (response.isSuccessful) {
                    eleccionesCache = response.body() ?: emptyList()
                    _uiState.value = VotacionUiState.EleccionesListas(eleccionesCache)
                } else {
                    _uiState.value = VotacionUiState.Error("No se pudieron cargar las elecciones")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun seleccionarEleccion(eleccion: EleccionDto) {
        viewModelScope.launch {
            val draft = draftActual ?: return@launch
            _uiState.value = VotacionUiState.Cargando
            try {
                val yaVotoResp = RetrofitClient.api.yaVoto(draft.cedula, eleccion.idEleccion)
                if (yaVotoResp.isSuccessful && yaVotoResp.body() == true) {
                    _uiState.value = VotacionUiState.YaVoto(eleccion.nombre)
                    return@launch
                }
            } catch (_: Exception) { /* sin conexión: continuar */ }
            val actualizado = draft.copy(
                idEleccion = eleccion.idEleccion,
                nombreEleccion = eleccion.nombre,
                estado = EstadoDraft.ELECCION_SELECCIONADA.name
            )
            votoDraftDao.actualizar(actualizado)
            draftActual = actualizado
            cargarCandidatos(eleccion)
        }
    }

    private fun cargarCandidatos(eleccion: EleccionDto) {
        viewModelScope.launch {
            _uiState.value = VotacionUiState.Cargando
            try {
                val candidatos = if (!eleccion.candidatos.isNullOrEmpty()) {
                    eleccion.candidatos
                } else {
                    val response = RetrofitClient.api.getCandidatos(eleccion.idEleccion)
                    if (response.isSuccessful) response.body() ?: emptyList()
                    else {
                        _uiState.value = VotacionUiState.Error("No se pudieron cargar los candidatos")
                        return@launch
                    }
                }
                _uiState.value = VotacionUiState.CandidatosListos(candidatos)
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun seleccionarCandidato(candidato: CandidatoDto) {
        viewModelScope.launch {
            val draft = draftActual ?: return@launch
            val actualizado = draft.copy(
                tipoSeleccion = "CANDIDATO",
                idSeleccion = candidato.idCandidato,
                nombreSeleccion = candidato.nombre,
                estado = EstadoDraft.CANDIDATO_SELECCIONADO.name
            )
            votoDraftDao.actualizar(actualizado)
            draftActual = actualizado
            _uiState.value = VotacionUiState.ListoParaConfirmar(
                draftId = actualizado.id,
                nombreCandidato = candidato.nombre,
                nombrePartido = candidato.nombrePartido
            )
        }
    }

    fun confirmarVoto() {
        viewModelScope.launch {
            val draft = draftActual ?: return@launch
            _uiState.value = VotacionUiState.Cargando
            try {
                val request = VotoRequest(
                    cedula = draft.cedula,
                    idEleccion = draft.idEleccion,
                    idMesa = draft.idMesa,
                    tipoSeleccion = draft.tipoSeleccion ?: "CANDIDATO",
                    idSeleccion = draft.idSeleccion ?: 0L
                )
                val response = RetrofitClient.api.votar(request)
                if (response.isSuccessful) {
                    votoDraftDao.actualizarEstado(draft.id, EstadoDraft.ENVIADO.name)
                    draftActual = null
                    _uiState.value = VotacionUiState.VotoRegistrado
                } else {
                    _uiState.value = VotacionUiState.Error("Error al registrar voto: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Sin conexión: ${e.message}")
            }
        }
    }

    fun reiniciar() {
        draftActual = null
        eleccionesCache = emptyList()
        _uiState.value = VotacionUiState.Idle
    }

    fun limpiarError() {
        _uiState.value = VotacionUiState.Idle
    }

    class Factory(
        private val prefs: PrefsManager,
        private val dao: VotoDraftDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VotacionViewModel(prefs, dao) as T
    }
}
