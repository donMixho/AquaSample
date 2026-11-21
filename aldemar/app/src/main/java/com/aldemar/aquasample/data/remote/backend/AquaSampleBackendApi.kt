package com.aldemar.aquasample.data.remote.backend

import com.aldemar.aquasample.data.local.entity.SampleEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Cliente REST del backend. Base URL configurable (por defecto http://10.0.2.2:8080/api/). */
interface AquaSampleBackendApi {
    @GET("samples") suspend fun list(): List<SampleDto>
    @GET("samples/{code}") suspend fun get(@Path("code") code: String): SampleDto
    @POST("samples") suspend fun create(@Body sample: SampleDto): Response<SampleDto>
    @PUT("samples/{code}") suspend fun update(@Path("code") code: String, @Body sample: SampleDto): Response<SampleDto>
    @PUT("samples/{code}/review") suspend fun review(@Path("code") code: String, @Body review: ReviewDto): Response<Unit>
}

data class ReviewDto(val reviewStatus: String, val supervisorComment: String?, val supervisorName: String?)

/** Metadatos de la muestra (la foto no se sube en este paso). [code] es la clave única anti-duplicados. */
data class SampleDto(
    val code: String,
    val concession: String?,
    val center: String,
    val train: String,
    val line: String,
    val sectionLengthCm: Int,
    val timestamp: Long,
    val operatorName: String,
    val manualCount: Int,
    val markerCoordinatesJson: String,
    val observations: String,
    val reviewStatus: String,
    val supervisorComment: String?,
    val supervisorName: String?
)

fun SampleEntity.toDto() = SampleDto(
    code, concession, center, train, line, sectionLengthCm, timestamp, operatorName, manualCount,
    markerCoordinatesJson, observations, reviewStatus.name, supervisorComment, supervisorName
)
