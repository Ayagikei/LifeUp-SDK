package net.lifeupapp.lifeup.http.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Settings private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enableCors: Boolean by BooleanPreference(prefs, KEY_ENABLE_CORS, false)

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_ENABLE_CORS = "enable_cors"

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
} 