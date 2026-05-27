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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

sealed class VotacionUiState {
    object Idle : VotacionUiState()
    object Cargando : VotacionUiState()
    data class CiudadanoIdentificado(val nombre: String) : VotacionUiState()
    data class CiudadanoDomicilio(val nombre: String) : VotacionUiState()
    data class EleccionesListas(val elecciones: List<EleccionDto>) : VotacionUiState()
    data class CandidatosListos(val candidatos: List<CandidatoDto>) : VotacionUiState()
    data class ListoParaConfirmar(val draftId: String, val nombreCandidato: String, val nombrePartido: String?) : VotacionUiState()
    object VotoRegistrado : VotacionUiState()
    data class Error(val mensaje: String) : VotacionUiState()
    data class DraftPendiente(val nombre: String, val eleccion: String) : VotacionUiState()
    data class YaVoto(val nombreEleccion: String) : VotacionUiState()
    object ConectadoAlServidor : VotacionUiState()
    data class LoginRegistradorExito(val nombre: String) : VotacionUiState()
    data class LoginRegistradorError(val mensaje: String) : VotacionUiState()
    object KickedPorServidor : VotacionUiState()
}

class VotacionViewModel(
    private val prefs: PrefsManager,
    private val votoDraftDao: VotoDraftDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<VotacionUiState>(VotacionUiState.Idle)
    val uiState: StateFlow<VotacionUiState> = _uiState

    // ─── Conectividad al servidor ─────────────────────────────────────────────
    private val _serverOnline = MutableStateFlow<Boolean?>(null)
    val serverOnline: StateFlow<Boolean?> = _serverOnline

    private val _autoSincronizando = MutableStateFlow(false)
    val autoSincronizando: StateFlow<Boolean> = _autoSincronizando

    val serverUrl: String get() = prefs.serverUrl
    val tipoMesa: String get() = prefs.tipoMesa
    val idMesaActual: Long get() = prefs.idMesa
    val centroActual: String get() = prefs.centro

    private var monitoringJob: Job? = null
    private var kickPollJob: Job? = null

    /** Inicia el monitoreo periódico del servidor (cada 8 s). Idempotente. */
    fun iniciarMonitoreo() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (true) {
                _serverOnline.value = chequearServidor()
                delay(8_000L)
            }
        }
        if (prefs.isConfigured) iniciarKickPolling()
    }

    /** Detiene el monitoreo periódico. */
    fun detenerMonitoreo() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /** Registra este dispositivo en el servidor con su nombre, mesa, tipo e IP local. */
    fun registrarDispositivo() {
        viewModelScope.launch {
            val ipLocal = obtenerIpLocal()
            prefs.ipLocal = ipLocal ?: ""
            try {
                val request = com.vote4tech.urna2.data.remote.dto.DispositivoRegistroRequest(
                    nombreDispositivo = android.os.Build.MODEL,
                    idMesa = prefs.idMesa,
                    tipoMesa = prefs.tipoMesa,
                    centro = prefs.centro,
                    ipLocal = ipLocal
                )
                RetrofitClient.api.registrarDispositivo(request)
            } catch (_: Exception) {}
            iniciarKickPolling()
        }
    }

    /** Inicia el polling periódico (cada 15 s) que detecta si el admin expulsó este dispositivo. */
    fun iniciarKickPolling() {
        kickPollJob?.cancel()
        kickPollJob = viewModelScope.launch {
            while (true) {
                delay(15_000L)
                try {
                    val deviceIp = prefs.ipLocal.ifBlank { null }
                    val resp = RetrofitClient.api.getMiEstado(deviceIp)
                    if (resp.isSuccessful && resp.body()?.activo == false) {
                        manejarKick()
                        break
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun obtenerIpLocal(): String? {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()?.toList() ?: return null
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in intf.inetAddresses.toList()) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (_: Exception) { null }
    }

    private fun manejarKick() {
        kickPollJob?.cancel()
        kickPollJob = null
        prefs.resetConfig()
        _uiState.value = VotacionUiState.KickedPorServidor
    }

    /** Dispara una verificación inmediata del servidor. */
    fun verificarAhora() {
        viewModelScope.launch {
            _serverOnline.value = null   // "verificando"
            _serverOnline.value = chequearServidor()
        }
    }

    private suspend fun chequearServidor(): Boolean = withContext(Dispatchers.IO) {
        val url = prefs.serverUrl
        if (url.isBlank()) return@withContext false
        try {
            val parsed = java.net.URL(url)
            val host = parsed.host
            val port = if (parsed.port > 0) parsed.port else 8081
            Socket().use { s ->
                s.connect(InetSocketAddress(host, port), 2500)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    private var draftActual: VotoDraftEntity? = null
    private var eleccionesCache: List<EleccionDto> = emptyList()

    val nombreCiudadanoActual: String get() = draftActual?.nombreCiudadano ?: ""

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
                    if (ciudadano.habilitadoDomicilio) {
                        _uiState.value = VotacionUiState.CiudadanoDomicilio(ciudadano.nombre)
                        return@launch
                    }
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
                } else when (response.code()) {
                    404 -> _uiState.value = VotacionUiState.Error("Cédula no registrada en este sistema de votación")
                    403 -> _uiState.value = VotacionUiState.Error("Acceso denegado por el servidor")
                    503 -> _uiState.value = VotacionUiState.Error("Sin conexión: el servidor no está disponible en este momento")
                    else -> _uiState.value = VotacionUiState.Error("El servidor respondió con error ${response.code()}")
                }
            } catch (e: java.io.IOException) {
                _uiState.value = VotacionUiState.Error("Sin conexión: no se pudo alcanzar el servidor")
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Error inesperado: ${e.localizedMessage}")
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
                    _uiState.value = VotacionUiState.Error("El servidor respondió con error ${response.code()} al cargar las elecciones")
                }
            } catch (e: java.io.IOException) {
                _uiState.value = VotacionUiState.Error("Sin conexión: no se pudieron cargar las elecciones del servidor")
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Error inesperado al cargar elecciones: ${e.localizedMessage}")
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
                } else when (response.code()) {
                    400 -> _uiState.value = VotacionUiState.Error("Voto no registrado: datos inválidos o cédula ya votó en esta elección")
                    409 -> _uiState.value = VotacionUiState.Error("Esta cédula ya tiene un voto registrado en esta elección")
                    503 -> _uiState.value = VotacionUiState.Error("Sin conexión: el servidor no está disponible — voto no registrado")
                    else -> _uiState.value = VotacionUiState.Error("Error del servidor al registrar voto (${response.code()})")
                }
            } catch (e: java.io.IOException) {
                _uiState.value = VotacionUiState.Error("Sin conexión: el voto no fue registrado — verifique la red e intente de nuevo")
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("Error inesperado al registrar voto: ${e.localizedMessage}")
            }
        }
    }

    fun prepararNuevaEleccionMismoCiudadano() {
        viewModelScope.launch {
            val old = draftActual ?: return@launch
            val newDraft = VotoDraftEntity(
                id = UUID.randomUUID().toString(),
                cedula = old.cedula,
                nombreCiudadano = old.nombreCiudadano,
                idMesa = old.idMesa
            )
            votoDraftDao.limpiarEnviados()
            votoDraftDao.insertar(newDraft)
            draftActual = newDraft
            // EleccionScreen llama volverAElecciones() al entrar — no se duplica aquí
        }
    }

    /** Muestra el caché de elecciones si existe; si no, hace fetch. Usar al volver a EleccionScreen. */
    fun volverAElecciones() {
        if (eleccionesCache.isNotEmpty()) {
            _uiState.value = VotacionUiState.EleccionesListas(eleccionesCache)
        } else {
            cargarElecciones()
        }
    }

    fun sincronizarAuto() {
        if (_autoSincronizando.value) return
        viewModelScope.launch {
            _autoSincronizando.value = true
            try {
                RetrofitClient.api.syncDescargar()
                RetrofitClient.api.syncSubir()
            } catch (_: Exception) { /* servidor local puede no ser alcanzable aún */ }
            finally {
                _autoSincronizando.value = false
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

    /** Verifica conectividad al servidor y emite ConectadoAlServidor (true) o false via booleano de retorno */
    fun verificarConectividadParaConfig() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.ping()
                if (response.isSuccessful) {
                    _uiState.value = VotacionUiState.ConectadoAlServidor
                } else {
                    _uiState.value = VotacionUiState.Error("sin_conexion")
                }
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.Error("sin_conexion")
            }
        }
    }

    fun loginRegistrador(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = VotacionUiState.Cargando
            val req = com.vote4tech.urna2.data.remote.dto.LoginRequest(username, password)
            // Intentar primero como registrador
            try {
                val resp = RetrofitClient.api.loginRegistrador(req)
                if (resp.isSuccessful && resp.body()?.exito == true) {
                    _uiState.value = VotacionUiState.LoginRegistradorExito(resp.body()!!.nombre ?: "Registrador")
                    return@launch
                }
            } catch (e: java.io.IOException) {
                _uiState.value = VotacionUiState.LoginRegistradorError("Sin conexión: no se pudo alcanzar el servidor")
                return@launch
            } catch (_: Exception) {}
            // Intentar como jurado
            try {
                val resp = RetrofitClient.api.loginJurado(req)
                if (resp.isSuccessful && resp.body()?.exito == true) {
                    _uiState.value = VotacionUiState.LoginRegistradorExito(resp.body()!!.nombre ?: "Jurado")
                    return@launch
                }
                _uiState.value = VotacionUiState.LoginRegistradorError("Usuario o contraseña incorrectos")
            } catch (e: java.io.IOException) {
                _uiState.value = VotacionUiState.LoginRegistradorError("Sin conexión: no se pudo alcanzar el servidor")
            } catch (e: Exception) {
                _uiState.value = VotacionUiState.LoginRegistradorError("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun seleccionarVotoBlanco() {
        viewModelScope.launch {
            val draft = draftActual ?: return@launch
            val actualizado = draft.copy(
                tipoSeleccion = "BLANCO",
                idSeleccion = 0L,
                nombreSeleccion = "Voto en Blanco",
                estado = EstadoDraft.CANDIDATO_SELECCIONADO.name
            )
            votoDraftDao.actualizar(actualizado)
            draftActual = actualizado
            _uiState.value = VotacionUiState.ListoParaConfirmar(
                draftId = actualizado.id,
                nombreCandidato = "Voto en Blanco",
                nombrePartido = null
            )
        }
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
