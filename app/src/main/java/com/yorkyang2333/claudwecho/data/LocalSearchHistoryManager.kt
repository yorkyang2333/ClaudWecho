package com.yorkyang2333.claudwecho.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalSearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    
    companion object {
        private const val KEY_HISTORY_QUERIES = "history_queries"
        private const val MAX_HISTORY = 20
    }

    fun addQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val current = getHistory().toMutableList()
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
        if (current.size > MAX_HISTORY) {
            current.subList(MAX_HISTORY, current.size).clear()
        }
        saveHistory(current)
    }

    fun getHistory(): List<String> {
        val jsonStr = prefs.getString(KEY_HISTORY_QUERIES, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_QUERIES).apply()
    }

    private fun saveHistory(queries: List<String>) {
        try {
            val jsonStr = json.encodeToString(queries)
            prefs.edit().putString(KEY_HISTORY_QUERIES, jsonStr).apply()
        } catch (e: Exception) {
            // Error encoding queries
        }
    }
}
