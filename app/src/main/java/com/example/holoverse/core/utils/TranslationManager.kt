package com.example.holoverse.core.utils

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val translators = mutableMapOf<String, Translator>()
    private val translationCache = LruCache<String, String>(500)

    suspend fun translate(
        text: String,
        sourceLang: String = TranslateLanguage.ENGLISH,
        targetLang: String
    ): String {
        if (sourceLang == targetLang || text.isBlank()) return text

        val cacheKey = "${sourceLang}_${targetLang}_$text"
        translationCache.get(cacheKey)?.let { return it }

        val langKey = "${sourceLang}_$targetLang"
        val translator = synchronized(translators) {
            translators.getOrPut(langKey) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()
                Translation.getClient(options)
            }
        }

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        return try {
            // Ensure model is downloaded
            translator.downloadModelIfNeeded(conditions).await()
            // Perform translation
            val result = translator.translate(text).await()
            translationCache.put(cacheKey, result)
            result
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error translating text: ${e.message}", e)
            text // Fallback to original text
        }
    }

    fun close() {
        synchronized(translators) {
            translators.values.forEach { it.close() }
            translators.clear()
        }
    }
}
