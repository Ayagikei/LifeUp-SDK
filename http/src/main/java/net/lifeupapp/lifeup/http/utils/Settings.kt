package net.lifeupapp.lifeup.http.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Settings private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enableCors: Boolean by BooleanPreference(prefs, KEY_ENABLE_CORS, false)
    var wakeLockDuration: Int by IntPreference(prefs, KEY_WAKE_LOCK_DURATION, 10)
    var customPort: Int by IntPreference(prefs, KEY_CUSTOM_PORT, 0)
    var apiToken: String by StringPreference(prefs, KEY_API_TOKEN, "")

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_ENABLE_CORS = "enable_cors"
        private const val KEY_WAKE_LOCK_DURATION = "wake_lock_duration"
        private const val KEY_CUSTOM_PORT = "custom_port"
        private const val KEY_API_TOKEN = "api_token"

        const val MIN_WAKE_LOCK_DURATION = 1
        const val MAX_WAKE_LOCK_DURATION = 60
        const val MIN_PORT = 1024
        const val MAX_PORT = 65535
        const val DEFAULT_PORT = 13276

        @Volatile
        private var instance: Settings? = null

        fun getInstance(context: Context): Settings {
            return instance ?: synchronized(this) {
                instance ?: Settings(context.applicationContext).also { instance = it }
            }
        }
    }

    private class BooleanPreference(
        private val preferences: SharedPreferences,
        private val name: String,
        private val defaultValue: Boolean
    ) : ReadWriteProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
            return preferences.getBoolean(name, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            preferences.edit().putBoolean(name, value).apply()
        }
    }

    private class IntPreference(
        private val preferences: SharedPreferences,
        private val name: String,
        private val defaultValue: Int
    ) : ReadWriteProperty<Any, Int> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Int {
            return preferences.getInt(name, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
            preferences.edit().putInt(name, value).apply()
        }
    }

    private class StringPreference(
        private val preferences: SharedPreferences,
        private val name: String,
        private val defaultValue: String
    ) : ReadWriteProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String {
            return preferences.getString(name, defaultValue) ?: defaultValue
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            preferences.edit().putString(name, value).apply()
        }
    }
} 