package com.aldemar.aquasample.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aldemar.aquasample.data.local.dao.SampleDao
import com.aldemar.aquasample.data.local.entity.CenterEntity
import com.aldemar.aquasample.data.local.entity.SampleEntity

@Database(entities = [SampleEntity::class, CenterEntity::class], version = 1, exportSchema = true)
abstract class AquaSampleDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    companion object {
        @Volatile private var instance: AquaSampleDatabase? = null
        fun getDatabase(context: Context): AquaSampleDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext,
                AquaSampleDatabase::class.java, "aquasample_aldemar.db")
                .build().also { instance = it }
        }
    }
}
