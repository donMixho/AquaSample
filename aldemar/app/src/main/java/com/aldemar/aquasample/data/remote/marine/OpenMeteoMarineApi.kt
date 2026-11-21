package com.aldemar.aquasample.data.remote.marine

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** Open-Meteo Marine (sin API key): https://marine-api.open-meteo.com/v1/marine */
interface OpenMeteoMarineApi {
    @GET("v1/marine")
    suspend fun current(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "wave_height,wave_direction,wave_period,sea_surface_temperature",
        @Query("timezone") timezone: String = "auto"
    ): MarineResponse

    companion object { const val BASE_URL = "https://marine-api.open-meteo.com/" }
}

data class MarineResponse(val current: MarineCurrent? = null)

data class MarineCurrent(
    val time: String? = null,
    @SerializedName("wave_height") val waveHeight: Double? = null,
    @SerializedName("wave_direction") val waveDirection: Double? = null,
    @SerializedName("wave_period") val wavePeriod: Double? = null,
    @SerializedName("sea_surface_temperature") val seaSurfaceTemperature: Double? = null
)

data class MarineLocation(val latitude: Double, val longitude: Double, val name: String) {
    companion object { val DEFAULT = MarineLocation(-41.47, -72.94, "Los Lagos · Puerto Montt (aprox.)") }
}

/** Condiciones guardadas/mostradas. [fetchedAt] = momento de la consulta (epoch ms). */
data class MarineConditions(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAt: Long,
    val waveHeightM: Double?,
    val waveDirectionDeg: Double?,
    val wavePeriodS: Double?,
    val seaTempC: Double?
)

/** Resultado para la UI: [stale] = datos de caché porque la consulta falló (sin internet/timeout). */
data class MarineState(val conditions: MarineConditions?, val stale: Boolean, val error: String?)
