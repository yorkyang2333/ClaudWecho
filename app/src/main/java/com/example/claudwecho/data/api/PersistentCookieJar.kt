package com.example.claudwecho.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(context: Context) : CookieJar {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CookiePrefs", Context.MODE_PRIVATE)
    private val cookies = ConcurrentHashMap<String, MutableList<Cookie>>()

    init {
        // Clear corrupted cookies from previous versions
        if (!sharedPreferences.getBoolean("is_corrupted_cleared_v2", false)) {
            sharedPreferences.edit().clear().putBoolean("is_corrupted_cleared_v2", true).apply()
        }

        // Load cookies from shared preferences
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (value is String && key != "is_corrupted_cleared_v2") {
                val parsedCookies = value.split("\n").mapNotNull { Cookie.parse("http://$key".toHttpUrlOrNull()!!, it) }
                if (parsedCookies.isNotEmpty()) {
                    cookies[key] = parsedCookies.toMutableList()
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
        val host = url.host
        val currentCookies = cookies[host] ?: mutableListOf()
        
        // Update existing cookies or add new ones
        newCookies.forEach { newCookie ->
            currentCookies.removeAll { it.name == newCookie.name }
            currentCookies.add(newCookie)
        }
        
        cookies[host] = currentCookies
        
        // Save to shared preferences
        val cookieString = currentCookies.joinToString("\n") { it.toString() }
        sharedPreferences.edit().putString(host, cookieString).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val validCookies = mutableListOf<Cookie>()
        val hostCookies = cookies[url.host] ?: mutableListOf()
        
        val iterator = hostCookies.iterator()
        var changed = false
        while (iterator.hasNext()) {
            val cookie = iterator.next()
            if (cookie.expiresAt < System.currentTimeMillis()) {
                iterator.remove()
                changed = true
            } else {
                validCookies.add(cookie)
            }
        }

        if (changed) {
            val cookieString = hostCookies.joinToString("\n") { it.toString() }
            sharedPreferences.edit().putString(url.host, cookieString).apply()
        }

        return validCookies
    }
}
