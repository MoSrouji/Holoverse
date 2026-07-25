package com.example.holoverse.course.domain

enum class AdCardStyle {
    STYLE_1,
    STYLE_2,
    STYLE_3
}

data class BoostedCourse(
    val courseId: String = "",
    val adCardStyle: AdCardStyle = AdCardStyle.STYLE_1,
    val planDuration: String = "",
    val endTimestamp: Long = 0L,
    val courseName: String = "",
    val courseImageUrl: String = "",
    val courseDescription: String = "",
    val instructorName: String = ""
)

