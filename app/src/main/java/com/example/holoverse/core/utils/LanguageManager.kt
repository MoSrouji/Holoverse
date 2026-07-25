package com.example.holoverse.core.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.holoverse.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {

    fun applyLanguage() {
        val languageCode = preferenceManager.getLanguage() ?: return
        setLocale(languageCode)
    }

    fun setLocale(languageCode: String?) {
        preferenceManager.saveLanguage(languageCode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeList = if (languageCode != null) {
                LocaleList.forLanguageTags(languageCode)
            } else {
                LocaleList.getEmptyLocaleList()
            }
            context.getSystemService(LocaleManager::class.java).applicationLocales = localeList
        } else {
            val appLocale: LocaleListCompat = if (languageCode != null) {
                LocaleListCompat.forLanguageTags(languageCode)
            } else {
                LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun getSelectedLanguageName(): String {
        val code = preferenceManager.getLanguage()
        return when (code) {
            "ar" -> context.getString(R.string.arabic)
            "en" -> context.getString(R.string.english)
            "es" -> context.getString(R.string.spanish)
            "it" -> context.getString(R.string.italian)
            else -> context.getString(R.string.device_language)
        }
    }
}

