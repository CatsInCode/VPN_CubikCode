package com.example.vpn_cubikcode.data

import android.content.Context

class ConfigRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveConfig(json: String) {
        prefs.edit().putString(KEY_XRAY_JSON, json).apply()
    }

    fun getConfig(): String = prefs.getString(KEY_XRAY_JSON, "").orEmpty()

    companion object {
        private const val PREF_NAME = "vpn_config_store"
        private const val KEY_XRAY_JSON = "xray_json"
    }
}
