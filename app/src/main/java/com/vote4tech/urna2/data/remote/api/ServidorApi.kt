package com.vote4tech.urna2.data.remote.api

import com.vote4tech.urna2.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ServidorApi {
    @GET("config/ping")
    suspend fun ping(): Response<ServerInfoDto>

    @POST("config/registrador/login")
    suspend fun loginRegistrador(@Body request: LoginRequest): Response<LoginResponse>

    @GET("ciudadano/{cedula}")
    suspend fun getCiudadano(@Path("cedula") cedula: String): Response<CiudadanoDto>

    @GET("eleccion/activas")
    suspend fun getEleccionesActivas(): Response<List<EleccionDto>>

    @GET("eleccion/{id}/candidatos")
    suspend fun getCandidatos(@Path("id") idEleccion: Long): Response<List<CandidatoDto>>

    @GET("voto/ya-voto/{cedula}/{idEleccion}")
    suspend fun yaVoto(
        @Path("cedula") cedula: String,
        @Path("idEleccion") idEleccion: Long
    ): Response<Boolean>

    @POST("voto/votar")
    suspend fun votar(@Body request: VotoRequest): Response<VotoResponse>

    @GET("config/mesas")
    suspend fun getMesas(): Response<List<MesaInfoDto>>
}
