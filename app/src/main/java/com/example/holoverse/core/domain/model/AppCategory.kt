package com.example.holoverse.core.domain.model

import androidx.annotation.StringRes
import com.example.holoverse.R
import kotlinx.serialization.Serializable

@Serializable
enum class AppCategory(@StringRes val titleRes: Int, @StringRes val specializations: List<Int>) {
    COMPUTER_SCIENCE(
        R.string.category_computer_science,
        listOf(
            R.string.category_computer_science,
            R.string.spec_cs_programming,
            R.string.category_web_development,
            R.string.category_mobile_development,
            R.string.category_data_science,
            R.string.category_ai,
            R.string.category_seo_marketing
        )
    ),
    BUSINESS(
        R.string.category_business,
        listOf(
            R.string.spec_business_studies,
            R.string.category_economics,
            R.string.spec_business_accounting,
            R.string.category_entrepreneurship,
            R.string.spec_business_finance,
            R.string.category_hr_management,
            R.string.category_office_productivity
        )
    ),
    MATHEMATICS(
        R.string.category_mathematics,
        listOf(
            R.string.spec_math_elementary,
            R.string.spec_math_algebra,
            R.string.spec_math_geometry,
            R.string.spec_math_calculus,
            R.string.spec_math_statistics,
            R.string.spec_math_competition
        )
    ),
    SCIENCE(
        R.string.category_science,
        listOf(
            R.string.spec_science_physics,
            R.string.spec_science_chemistry,
            R.string.spec_science_biology,
            R.string.spec_science_environmental,
            R.string.spec_science_earth,
            R.string.spec_science_ap
        )
    ),
    LANGUAGES(
        R.string.category_languages,
        listOf(
            R.string.spec_languages_english,
            R.string.spec_languages_esl,
            R.string.spec_languages_foreign,
            R.string.spec_languages_reading,
            R.string.spec_languages_writing,
            R.string.category_language_learning
        )
    ),
    HUMANITIES(
        R.string.category_humanities,
        listOf(
            R.string.spec_humanities_history,
            R.string.spec_humanities_social_studies,
            R.string.spec_humanities_geography,
            R.string.category_philosophy,
            R.string.category_psychology
        )
    ),
    ARTS(
        R.string.category_arts,
        listOf(
            R.string.spec_arts_music,
            R.string.category_graphic_design,
            R.string.category_3d_design,
            R.string.category_photography,
            R.string.spec_arts_art_design,
            R.string.spec_arts_drama_theater,
            R.string.spec_arts_creative_writing
        )
    ),
    HEALTH_FITNESS(
        R.string.category_health_fitness,
        listOf(
            R.string.category_health_fitness,
            R.string.category_personal_development,
            R.string.spec_health_physical_education,
            R.string.spec_health_nutrition,
            R.string.spec_health_mental_wellness
        )
    ),
    TEST_PREP(
        R.string.category_test_prep,
        listOf(
            R.string.spec_test_prep_sat_act,
            R.string.spec_test_prep_college_entrance,
            R.string.spec_test_prep_graduate,
            R.string.spec_test_prep_strategies
        )
    ),
    SPECIAL_EDUCATION(
        R.string.category_special_education,
        listOf(
            R.string.spec_special_ed_learning_disabilities,
            R.string.spec_special_ed_autism,
            R.string.spec_special_ed_dyslexia,
            R.string.spec_special_ed_inclusive
        )
    ),
    OTHER(
        R.string.Categories,
        listOf(
            R.string.category_elementary_education,
            R.string.spec_other_specialized,
            R.string.spec_other_homework,
            R.string.category_study_skills,
            R.string.spec_other_mentorship
        )
    );

    companion object {
        fun fromString(value: String): AppCategory {
            val normalizedValue = value.trim().replace(" ", "_").replace("&", "").replace("__", "_").uppercase()
            return try {
                entries.find { it.name == normalizedValue } ?: OTHER
            } catch (e: Exception) {
                // Fallback for more complex cases or legacy strings
                entries.find { it.name.contains(normalizedValue) || normalizedValue.contains(it.name) } ?: OTHER
            }
        }

        fun getAllCategoryNames(): List<String> {
            return entries.map { it.name }
        }
    }
}
