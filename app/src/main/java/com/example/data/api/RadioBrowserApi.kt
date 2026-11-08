package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RadioBrowserApi {

    @GET("stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("tag") tag: String? = null,
        @Query("country") country: String? = null,
        @Query("countrycode") countrycode: String? = null,
        @Query("language") language: String? = null,
        @Query("limit") limit: Int = 40,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hidebroken: Boolean = true
    ): List<RadioStationDto>

    @GET("stations/topclick")
    suspend fun getTopStations(
        @Query("limit") limit: Int = 30,
        @Query("hidebroken") hidebroken: Boolean = true
    ): List<RadioStationDto>

    @GET("stations/bycountry/germany")
    suspend fun getGermanyStations(
        @Query("limit") limit: Int = 30,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioStationDto>

    companion object {
        private const val BASE_URL = "https://de1.api.radio-browser.info/json/"

        fun create(): RadioBrowserApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RadioBrowserApi::class.java)
        }
    }
}
