package com.vote4tech.urna2

import android.app.Application
import com.vote4tech.urna2.data.local.db.AppDatabase
import com.vote4tech.urna2.data.remote.RetrofitClient
import com.vote4tech.urna2.util.PrefsManager

class UrnaApplication : Application() {
    lateinit var prefs: PrefsManager
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        database = AppDatabase.getInstance(this)
        if (prefs.serverUrl.isNotBlank()) {
            RetrofitClient.init(prefs.serverUrl)
        }
    }
}
