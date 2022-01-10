package com.tlrm.mobile.whapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


class ServiceGenerator {
    companion object Factory {
        private val BASE_URL = "https://rms-mobile-indo-uat.transnational-lk.com"

        var okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        private val builder = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())

        val retrofit: Retrofit = builder.build()
        private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
        private val httpClient = OkHttpClient.Builder()

        fun <S> createService(serviceClass: Class<S>?): S {
            if (!httpClient.interceptors().contains(loggingInterceptor)) {
                httpClient.addInterceptor(loggingInterceptor)
                builder.client(httpClient.build())
            }
            return retrofit.create(serviceClass)
        }
    }
}