package com.vote4tech.urna.data.remote

import com.vote4tech.urna.data.remote.api.ServidorApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl: String = "http://192.168.1.100:8080/"
    private var retrofit: Retrofit? = null

    fun init(url: String) {
        if (!url.endsWith("/")) {
            baseUrl = "$url/"
        } else {
            baseUrl = url
        }
        retrofit = null // Forzar recreación
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    val api: ServidorApi get() = getRetrofit().create(ServidorApi::class.java)
}
