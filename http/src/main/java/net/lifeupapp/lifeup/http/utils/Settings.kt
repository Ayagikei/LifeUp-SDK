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

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_ENABLE_CORS = "enable_cors"
        private const val KEY_WAKE_LOCK_DURATION = "wake_lock_duration"

        const val MIN_WAKE_LOCK_DURATION = 1
        const val MAX_WAKE_LOCK_DURATION = 60

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
} 