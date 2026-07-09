package com.example.holoverse.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.holoverse.auth.domain.entities.User
import com.example.holoverse.auth.domain.entities.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import javax.inject.Inject


class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun saveUser(user: User, isProfileComplete: Boolean = true) {
        val userJson = when (user) {
            is User.Student -> json.encodeToString(user)
            is User.Mentor -> json.encodeToString(user)
        }
        sharedPreferences.edit {
            putString(KEY_USER, userJson)
            putString(KEY_USER_TYPE, user.accountType.name)
            putBoolean(KEY_PROFILE_COMPLETE, isProfileComplete)
        }
    }

    fun isProfileComplete(): Boolean {
        return sharedPreferences.getBoolean(KEY_PROFILE_COMPLETE, false)
    }

    fun setProfileComplete(isComplete: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_PROFILE_COMPLETE, isComplete)
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

    fun getNotificationSetting(key: String, defaultValue: Boolean = true): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setNotificationSetting(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    fun getRingtone(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun setRingtone(key: String, uri: String) {
        sharedPreferences.edit {
            putString(key, uri)
        }
    }

    fun getRingtoneName(key: String): String {
        return sharedPreferences.getString(key, "Default") ?: "Default"
    }

    fun setRingtoneName(key: String, name: String) {
        sharedPreferences.edit {
            putString(key, name)
        }
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

    val userFlow: Flow<User?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_USER || key == KEY_USER_TYPE) {
                trySend(getUser())
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart { emit(getUser()) }

    fun clearData() {
        sharedPreferences.edit {
            remove(KEY_USER)
            remove(KEY_USER_TYPE)
            remove(KEY_PROFILE_COMPLETE)
        }
    }

    companion object {
        private const val PREF_NAME = "holoverse_prefs"
        private const val KEY_USER = "user_data"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"

        const val KEY_MESSAGE_NOTIFICATIONS = "message_notifications"
        const val KEY_CALL_NOTIFICATIONS = "call_notifications"
        const val KEY_TEACHER_NOTIFICATIONS = "teacher_notifications"
        const val KEY_MESSAGE_RINGTONE = "message_ringtone"
        const val KEY_MESSAGE_RINGTONE_NAME = "message_ringtone_name"
        const val KEY_CALL_RINGTONE = "call_ringtone"
        const val KEY_CALL_RINGTONE_NAME = "call_ringtone_name"
    }
}
