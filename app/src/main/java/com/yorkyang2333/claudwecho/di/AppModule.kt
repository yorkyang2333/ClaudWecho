package com.yorkyang2333.claudwecho.di

import com.yorkyang2333.claudwecho.data.api.NeteaseApi
import com.yorkyang2333.claudwecho.data.LoginRepository
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.ui.login.LoginViewModel
import com.yorkyang2333.claudwecho.ui.main.MainViewModel
import com.yorkyang2333.claudwecho.ui.player.PlayerViewModel
import com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailViewModel
import com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel
import com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendViewModel
import com.yorkyang2333.claudwecho.data.api.PersistentCookieJar
import com.yorkyang2333.claudwecho.BuildConfig
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
    single { com.yorkyang2333.claudwecho.data.LocalRecentPlaysManager(androidContext()) }
    single { com.yorkyang2333.claudwecho.data.PlaybackStateManager(androidContext()) }
    single { LoginRepository(get()) }
    single { MainRepository(get(), get()) }
    
    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { PlayerViewModel(androidContext(), get(), get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recent.RecentlyPlayedViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.settings.SettingsViewModel(androidContext(), get(), get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.search.SearchViewModel(get()) }
}
