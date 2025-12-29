package com.rosan.ruto.util

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "ruto_settings"
    private const val KEY_HOST_URL = "host_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MODEL_ID = "model_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSettings(context: Context, hostUrl: String, apiKey: String, modelId: String) {
        getPrefs(context).edit()
            .putString(KEY_HOST_URL, hostUrl)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_MODEL_ID, modelId)
            .apply()
    }

    fun getHostUrl(context: Context): String {
        return getPrefs(context).getString(KEY_HOST_URL, "https://open.bigmodel.cn/api/paas/v4/") ?: ""
    }

    fun getApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_API_KEY, "") ?: ""
    }

    fun getModelId(context: Context): String {
        return getPrefs(context).getString(KEY_MODEL_ID, "autoglm-phone") ?: ""
    }

    fun areSettingsConfigured(context: Context): Boolean {
        val prefs = getPrefs(context)
        val hostUrl = prefs.getString(KEY_HOST_URL, null)
        val apiKey = prefs.getString(KEY_API_KEY, null)
        val modelId = prefs.getString(KEY_MODEL_ID, null)
        return !hostUrl.isNullOrBlank() && !apiKey.isNullOrBlank() && !modelId.isNullOrBlank()
    }
}
