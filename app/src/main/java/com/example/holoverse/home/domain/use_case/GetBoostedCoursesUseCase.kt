package com.example.holoverse.home.domain.use_case

import com.example.holoverse.core.utils.PreferenceManager
import com.example.holoverse.core.utils.TranslationManager
import com.example.holoverse.course.domain.BoostedCourse
import com.example.holoverse.fetch.domain.FetchDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetBoostedCoursesUseCase @Inject constructor(
    private val fetchDataRepository: FetchDataRepository,
    private val translationManager: TranslationManager,
    private val preferenceManager: PreferenceManager
) {
    suspend operator fun invoke(): List<BoostedCourse> = withContext(Dispatchers.Default) {
        val rawBoosts = fetchDataRepository.fetchBoostedCourses()
        val targetLang = preferenceManager.getLanguage() ?: "en"

        if (targetLang == "en") {
            return@withContext rawBoosts
        }

        rawBoosts.map { boost ->
            boost.copy(
                courseName = translationManager.translate(boost.courseName, targetLang = targetLang),
                courseDescription = translationManager.translate(boost.courseDescription, targetLang = targetLang),
                instructorName = translationManager.translate(boost.instructorName, targetLang = targetLang)
            )
        }
    }
}
