package com.vote4tech.urna.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Almacena la configuración del dispositivo de urna:
 * - URL del servidor LAN
 * - Mesa asignada a este dispositivo
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("urna_config", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(v) = prefs.edit().putString(KEY_SERVER_URL, v).apply()

    var idMesa: Long
        get() = prefs.getLong(KEY_ID_MESA, 0L)
        set(v) = prefs.edit().putLong(KEY_ID_MESA, v).apply()

    var tipoMesa: String
        get() = prefs.getString(KEY_TIPO_MESA, "URNA") ?: "URNA"
        set(v) = prefs.edit().putString(KEY_TIPO_MESA, v).apply()

    /** La app está lista para operar cuando tiene URL y mesa configuradas. */
    val isConfigured: Boolean
        get() = serverUrl.isNotBlank() && idMesa > 0L

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_ID_MESA    = "id_mesa"
        private const val KEY_TIPO_MESA  = "tipo_mesa"
    }
}
