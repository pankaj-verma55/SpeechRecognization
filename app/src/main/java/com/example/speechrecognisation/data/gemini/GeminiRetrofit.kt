package com.example.speechrecognisation.data.gemini

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiRetrofit {

    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/"

    val api: GeminiApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GeminiApiService::class.java)
    }
}