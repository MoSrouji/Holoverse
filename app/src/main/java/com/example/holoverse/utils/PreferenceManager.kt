package com.example.holoverse.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart


class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun saveUser(user: User) {
        val userJson = when (user) {
            is User.Student -> json.encodeToString(user)
            is User.Mentor -> json.encodeToString(user)
        }
        sharedPreferences.edit {
            putString(KEY_USER, userJson)
            putString(KEY_USER_TYPE, user.accountType.name)
        }
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null) ?: return null
        val userType = sharedPreferences.getString(KEY_USER_TYPE, null) ?: return null

        return try {
            if (userType == UserType.Student.name) {
                json.decodeFromString<User.Student>(userJson)
            } else {
                json.decodeFromString<User.Mentor>(userJson)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveLanguage(languageCode: String?) {
        sharedPreferences.edit {
            putString(KEY_LANGUAGE, languageCode)
        }
    }

    fun getLanguage(): String? {
        return sharedPreferences.getString(KEY_LANGUAGE, null)
    }

    fun saveThemeMode(themeMode: String) {
        sharedPreferences.edit {
            putString(KEY_THEME_MODE, themeMode)
        }
    }

    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "system") ?: "system"
    }

    val themeModeFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_THEME_MODE) {
                trySend(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart { emit(getThemeMode()) }

    fun clearData() {
        sharedPreferences.edit { clear() }
    }

    companion object {
        private const val PREF_NAME = "holoverse_prefs"
        private const val KEY_USER = "user_data"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
