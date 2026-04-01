package ru.netology.nmadia_hw.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmadia_hw.BuildConfig
import ru.netology.nmadia_hw.api.ApiConfig
import ru.netology.nmadia_hw.api.PostsApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .build()

    @Provides
    @Singleton
    fun providePostsApiService(
        retrofit: Retrofit,
    ): PostsApiService = retrofit.create(PostsApiService::class.java)
}