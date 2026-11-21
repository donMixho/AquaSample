package com.aldemar.aquasample.data.remote.marine

import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface MarineCache {
    suspend fun location(): MarineLocation
    suspend fun cached(): MarineConditions?
    suspend fun save(conditions: MarineConditions)
}

class MarineRepository(
    private val api: OpenMeteoMarineApi,
    private val cache: MarineCache,
    private val clock: () -> Long = System::currentTimeMillis
) {
    /** Consulta la API; si falla (sin red, timeout, datos vacíos) devuelve la última caché. */
    suspend fun load(): MarineState {
        val loc = cache.location()
        return try {
            val c = api.current(loc.latitude, loc.longitude).current
                ?: error("Respuesta sin datos marinos")
            val result = MarineConditions(
                loc.name, loc.latitude, loc.longitude, clock(),
                c.waveHeight, c.waveDirection, c.wavePeriod, c.seaSurfaceTemperature
            )
            cache.save(result)
            MarineState(result, stale = false, error = null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MarineState(cache.cached(), stale = true, error = e.message ?: e.javaClass.simpleName)
        }
    }

    companion object {
        fun create(cache: MarineCache, baseUrl: String = OpenMeteoMarineApi.BASE_URL): MarineRepository {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).build()
            val api = Retrofit.Builder().baseUrl(baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(OpenMeteoMarineApi::class.java)
            return MarineRepository(api, cache)
        }
    }
}
