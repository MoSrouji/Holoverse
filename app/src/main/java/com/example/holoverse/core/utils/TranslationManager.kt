package com.example.holoverse.core.utils

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun translate(
        text: String,
        sourceLang: String = TranslateLanguage.ENGLISH,
        targetLang: String
    ): String {
        if (sourceLang == targetLang || text.isBlank()) return text

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        return try {
            // Ensure model is downloaded
            translator.downloadModelIfNeeded(conditions).await()
            // Perform translation
            translator.translate(text).await()
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error translating text: ${e.message}", e)
            text // Fallback to original text
        } finally {
            translator.close()
        }
    }
}
