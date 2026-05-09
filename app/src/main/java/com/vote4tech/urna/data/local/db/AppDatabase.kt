package com.vote4tech.urna.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vote4tech.urna.data.local.dao.VotoDraftDao
import com.vote4tech.urna.data.local.entity.VotoDraftEntity

@Database(
    entities = [VotoDraftEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun votoDraftDao(): VotoDraftDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "urna_db"
                ).build().also { INSTANCE = it }
            }
    }
}
