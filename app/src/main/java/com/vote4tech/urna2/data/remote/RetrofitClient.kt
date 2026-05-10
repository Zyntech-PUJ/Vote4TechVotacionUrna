package com.vote4tech.urna2.data.remote

import com.vote4tech.urna2.data.remote.api.ServidorApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl = "http://10.0.2.2:8081/"
    private var retrofit: Retrofit? = null

    fun init(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        baseUrl = normalized
        retrofit = null
    }

    private fun getRetrofit(): Retrofit {
        return retrofit ?: run {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }

    val api: ServidorApi
        get() = getRetrofit().create(ServidorApi::class.java)
}
