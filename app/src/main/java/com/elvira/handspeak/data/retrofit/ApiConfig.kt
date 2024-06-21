package com.elvira.handspeak.data.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
    companion object {
        private const val BASE_URL_STORAGE = "https://storage.googleapis.com/"
        private const val BASE_URL_HANDSPEAK = "https://handspeak-backend-x4ubec6fsq-et.a.run.app/"

        private fun getApiService(baseUrl: String): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }

        fun getStorageApiService(): ApiService {
            return getApiService(BASE_URL_STORAGE)
        }

        fun getHandspeakApiService(): ApiService {
            return getApiService(BASE_URL_HANDSPEAK)
        }
    }
}
