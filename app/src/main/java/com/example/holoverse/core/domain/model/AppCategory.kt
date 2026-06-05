package com.example.holoverse.core.domain.model

import com.example.holoverse.R
import kotlinx.serialization.Serializable

@Serializable
enum class AppCategory(val titleRes: Int, val specializations: List<String> = emptyList()) {
    THREE_D_DESIGN(R.string.category_3d_design),
    GRAPHIC_DESIGN(R.string.category_graphic_design),
    WEB_DEVELOPMENT(R.string.category_web_development),
    SEO_MARKETING(R.string.category_seo_marketing),
    FINANCE_ACCOUNTING(R.string.category_finance_accounting),
    PERSONAL_DEVELOPMENT(R.string.category_personal_development),
    OFFICE_PRODUCTIVITY(R.string.category_office_productivity),
    HR_MANAGEMENT(R.string.category_hr_management),
    DATA_SCIENCE(R.string.category_data_science),
    MOBILE_DEVELOPMENT(R.string.category_mobile_development),
    MUSIC(R.string.category_music),
    PHOTOGRAPHY(R.string.category_photography),
    BUSINESS(R.string.category_business, listOf("Business Studies", "Economics", "Accounting", "Entrepreneurship")),
    HEALTH_FITNESS(R.string.category_health_fitness),
    LANGUAGE_LEARNING(R.string.category_language_learning),
    AI(R.string.category_ai),
    MATHEMATICS(R.string.category_mathematics, listOf("Elementary Mathematics", "Algebra Specialist", "Geometry Expert", "Calculus Expert", "Statistics & Probability", "Math Competition Coach")),
    SCIENCE(R.string.category_science, listOf("Physics Specialist", "Chemistry Expert", "Biology Mentor", "Environmental Science", "Earth Science", "AP Science")),
    LANGUAGES(R.string.category_languages, listOf("English Language Arts", "ESL/EFL Specialist", "Foreign Languages", "Reading Specialist", "Writing Coach")),
    HUMANITIES(R.string.category_humanities, listOf("History Mentor", "Social Studies", "Geography", "Philosophy", "Psychology")),
    TEST_PREP(R.string.category_test_prep, listOf("SAT/ACT Prep Specialist", "College Entrance Exams", "Graduate Test Prep (GRE/GMAT)", "Standardized Test Strategies")),
    COMPUTER_SCIENCE(R.string.category_computer_science, listOf("Computer Science Mentor", "Programming Mentor", "Web Development", "Data Science")),
    ARTS(R.string.category_arts, listOf("Music Mentor", "Art & Design", "Drama/Theater", "Creative Writing")),
    SPECIAL_EDUCATION(R.string.category_special_education, listOf("Learning Disabilities Specialist", "Autism Spectrum Support", "Dyslexia Intervention", "Inclusive Education")),
    PHYSICS(R.string.category_physics),
    CHEMISTRY(R.string.category_chemistry),
    BIOLOGY(R.string.category_biology),
    HISTORY(R.string.category_history),
    PHILOSOPHY(R.string.category_philosophy),
    PSYCHOLOGY(R.string.category_psychology),
    ECONOMICS(R.string.category_economics),
    ENTREPRENEURSHIP(R.string.category_entrepreneurship),
    ELEMENTARY_EDUCATION(R.string.category_elementary_education),
    STUDY_SKILLS(R.string.category_study_skills),
    OTHER(R.string.Categories, listOf("Elementary Education", "Specialized Tutor", "Homework Help", "Study Skills"));

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
