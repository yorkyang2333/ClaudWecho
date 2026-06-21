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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
        val context: android.content.Context = get()
        val dynamicBaseUrlInterceptor = okhttp3.Interceptor { chain ->
            var request = chain.request()
            val prefs = context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
            val customUrlStr = prefs.getString("api_base_url", null)
            
            if (!customUrlStr.isNullOrBlank()) {
                val customHttpUrl = customUrlStr.toHttpUrlOrNull()
                if (customHttpUrl != null) {
                    val defaultBaseUrl = BuildConfig.API_BASE_URL.toHttpUrlOrNull()
                    if (defaultBaseUrl != null) {
                        val requestUrlStr = request.url.toString()
                        val defaultBaseUrlStr = defaultBaseUrl.toString()
                        if (requestUrlStr.startsWith(defaultBaseUrlStr)) {
                            val newUrlStr = customHttpUrl.toString().trimEnd('/') + "/" + requestUrlStr.substring(defaultBaseUrlStr.length).trimStart('/')
                            val newUrl = newUrlStr.toHttpUrlOrNull()
                            if (newUrl != null) {
                                request = request.newBuilder().url(newUrl).build()
                            }
                        }
                    }
                }
            }
            chain.proceed(request)
        }        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .cookieJar(get<PersistentCookieJar>())
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(logging)
            .build()
    }

    single {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val contentType = "application/json".toMediaType()
        
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
    viewModel { com.yorkyang2333.claudwecho.ui.profile.UserProfileViewModel(get(), get(), get()) }
    viewModel { PlayerViewModel(androidContext(), get(), get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recent.RecentlyPlayedViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.settings.SettingsViewModel(androidContext()) }
    viewModel { com.yorkyang2333.claudwecho.ui.search.SearchViewModel(get()) }
}
