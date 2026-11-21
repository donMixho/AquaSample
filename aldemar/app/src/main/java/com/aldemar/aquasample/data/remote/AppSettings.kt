package com.aldemar.aquasample.data.remote

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aldemar.aquasample.data.remote.marine.MarineCache
import com.aldemar.aquasample.data.remote.marine.MarineConditions
import com.aldemar.aquasample.data.remote.marine.MarineLocation
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("aquasample_settings")

/** Preferencias configurables (ubicación marina, URL del backend) y caché marino, en DataStore. */
class AppSettings(private val context: Context) : MarineCache {
    private val gson = Gson()

    suspend fun backendUrl(): String =
        normalizeBaseUrl(context.dataStore.data.first()[KEY_URL] ?: DEFAULT_BACKEND_URL)

    suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { it[KEY_URL] = normalizeBaseUrl(url) }
    }

    override suspend fun location(): MarineLocation = context.dataStore.data.first().let {
        MarineLocation(
            it[KEY_LAT] ?: MarineLocation.DEFAULT.latitude,
            it[KEY_LON] ?: MarineLocation.DEFAULT.longitude,
            it[KEY_NAME] ?: MarineLocation.DEFAULT.name
        )
    }

    suspend fun setLocation(loc: MarineLocation) {
        context.dataStore.edit { it[KEY_LAT] = loc.latitude; it[KEY_LON] = loc.longitude; it[KEY_NAME] = loc.name }
    }

    override suspend fun cached(): MarineConditions? =
        context.dataStore.data.map { it[KEY_CACHE] }.first()
            ?.let { runCatching { gson.fromJson(it, MarineConditions::class.java) }.getOrNull() }

    override suspend fun save(conditions: MarineConditions) {
        context.dataStore.edit { it[KEY_CACHE] = gson.toJson(conditions) }
    }

    companion object {
        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:8080/api/"
        private val KEY_URL = stringPreferencesKey("backend_url")
        private val KEY_LAT = doublePreferencesKey("marine_lat")
        private val KEY_LON = doublePreferencesKey("marine_lon")
        private val KEY_NAME = stringPreferencesKey("marine_name")
        private val KEY_CACHE: Preferences.Key<String> = stringPreferencesKey("marine_cache")

        /** Retrofit exige que la URL base termine en "/". */
        fun normalizeBaseUrl(url: String): String = url.trim().let { if (it.endsWith("/")) it else "$it/" }
    }
}
