package com.vote4tech.urna2.util

import android.content.Context

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("urna_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_ID_MESA = "id_mesa"
        private const val KEY_TIPO_MESA = "tipo_mesa"
        private const val KEY_CENTRO = "centro"
        private const val KEY_IP_LOCAL = "ip_local"
    }

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(v) = prefs.edit().putString(KEY_SERVER_URL, v).apply()

    var idMesa: Long
        get() = prefs.getLong(KEY_ID_MESA, 0L)
        set(v) = prefs.edit().putLong(KEY_ID_MESA, v).apply()

    var tipoMesa: String
        get() = prefs.getString(KEY_TIPO_MESA, "URNA") ?: "URNA"
        set(v) = prefs.edit().putString(KEY_TIPO_MESA, v).apply()

    var centro: String
        get() = prefs.getString(KEY_CENTRO, "") ?: ""
        set(v) = prefs.edit().putString(KEY_CENTRO, v).apply()

    var ipLocal: String
        get() = prefs.getString(KEY_IP_LOCAL, "") ?: ""
        set(v) = prefs.edit().putString(KEY_IP_LOCAL, v).apply()

    val isConfigured: Boolean get() = serverUrl.isNotBlank() && idMesa > 0L

    fun resetConfig() {
        prefs.edit()
            .putString(KEY_SERVER_URL, "")
            .putLong(KEY_ID_MESA, 0L)
            .putString(KEY_TIPO_MESA, "URNA")
            .putString(KEY_CENTRO, "")
            .putString(KEY_IP_LOCAL, "")
            .apply()
    }
}
