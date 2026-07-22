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
                val urlToParse = if (!customUrlStr.startsWith("http")) "http://$customUrlStr" else customUrlStr
                val customHttpUrl = urlToParse.toHttpUrlOrNull()
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
        val ipv4PrioritizedDns = object : okhttp3.Dns {
            override fun lookup(hostname: String): List<java.net.InetAddress> {
                val addresses = okhttp3.Dns.SYSTEM.lookup(hostname)
                // On Wear OS when connected via Bluetooth tethering/proxy, IPv6 routing is often broken or dropped by the phone bridge.
                // Prioritizing IPv4 addresses prevents socket connection hangs and timeouts over Bluetooth.
                return addresses.sortedBy { if (it is java.net.Inet4Address) 0 else 1 }
            }
        }
        val bluetoothCompatibilityRetryInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            var response: okhttp3.Response? = null
            var exception: java.io.IOException? = null
            val maxRetries = 2
            var tryCount = 0

            while (tryCount <= maxRetries && response == null) {
                try {
                    if (tryCount > 0) {
                        try { java.lang.Thread.sleep(500L * tryCount) } catch (e: InterruptedException) { /* ignore */ }
                    }
                    response = chain.proceed(request)
                } catch (e: java.io.IOException) {
                    exception = e
                    tryCount++
                    if (tryCount > maxRetries || request.method != "GET") {
                        throw e
                    }
                    android.util.Log.w("NetworkModule", "Network request failed over connection (try $tryCount/$maxRetries): ${e.message}, retrying...")
                }
            }
            response ?: throw (exception ?: java.io.IOException("Unknown network error"))
        }
        OkHttpClient.Builder()
            .cookieJar(get<PersistentCookieJar>())
            .dns(ipv4PrioritizedDns)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(15, 5, java.util.concurrent.TimeUnit.MINUTES))
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(bluetoothCompatibilityRetryInterceptor)
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
    single { com.yorkyang2333.claudwecho.data.LocalSearchHistoryManager(androidContext()) }
    single { LoginRepository(get()) }
    single { MainRepository(get(), get()) }
    
    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.profile.UserProfileViewModel(get(), get(), get()) }
    viewModel { PlayerViewModel(androidContext(), get(), get()) }
    viewModel { PlaylistDetailViewModel(get(), androidContext()) }
    viewModel { com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.recent.RecentlyPlayedViewModel(get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.settings.SettingsViewModel(androidContext(), get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.search.SearchViewModel(get(), get()) }
    viewModel { com.yorkyang2333.claudwecho.ui.songinfo.SongInfoViewModel(get()) }
}
