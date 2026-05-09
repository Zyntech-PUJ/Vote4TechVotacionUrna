package com.vote4tech.urna

import android.app.Application
import com.vote4tech.urna.data.local.db.AppDatabase
import com.vote4tech.urna.data.remote.RetrofitClient
import com.vote4tech.urna.util.PrefsManager

class UrnaApplication : Application() {

    lateinit var prefs: PrefsManager
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        database = AppDatabase.getInstance(this)
        // Inicializar Retrofit con la URL guardada
        if (prefs.serverUrl.isNotBlank()) {
            RetrofitClient.init(prefs.serverUrl)
        }
    }
}
