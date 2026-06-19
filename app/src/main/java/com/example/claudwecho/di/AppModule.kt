package com.example.claudwecho.di

import com.example.claudwecho.data.api.NeteaseApi
import com.example.claudwecho.data.LoginRepository
import com.example.claudwecho.data.MainRepository
import com.example.claudwecho.ui.login.LoginViewModel
import com.example.claudwecho.ui.main.MainViewModel
import com.example.claudwecho.ui.player.PlayerViewModel
import com.example.claudwecho.ui.playlist.PlaylistDetailViewModel
import com.example.claudwecho.ui.collection.MyCollectionViewModel
import com.example.claudwecho.ui.recommend.DailyRecommendViewModel
import com.example.claudwecho.data.api.PersistentCookieJar
import com.example.claudwecho.BuildConfig
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        PersistentCookieJar(androidContext())
    }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .cookieJar(get<PersistentCookieJar>())
            .addInterceptor(logging)
            .build()
    }

    single {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val contentType = "application/json".toMediaType()
        
        // User's own Vercel API instance
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    single { get<Retrofit>().create(NeteaseApi::class.java) }
    single { LoginRepository(get()) }
    single { MainRepository(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { PlayerViewModel(androidContext(), get()) }
    viewModel { com.example.claudwecho.ui.playlist.PlaylistDetailViewModel(get()) }
    viewModel { com.example.claudwecho.ui.collection.MyCollectionViewModel(get()) }
    viewModel { com.example.claudwecho.ui.recommend.DailyRecommendViewModel(get()) }
}
