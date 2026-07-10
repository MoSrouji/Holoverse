package com.example.holoverse.admin.domain.repository

import com.example.holoverse.core.utils.Response
import kotlinx.coroutines.flow.Flow

enum class Timeframe { DAY, WEEK, MONTH, YEAR }

interface AdminRepository {
    fun getStudentCount(): Flow<Response<Int>>
    fun getMentorCount(): Flow<Response<Int>>
    fun getCourseCount(): Flow<Response<Int>>
    fun getCoursesByCategory(): Flow<Response<Map<String, Int>>>
    fun getUserGrowthData(timeframe: Timeframe): Flow<Response<List<Pair<String, Int>>>>
    fun getTotalRevenue(): Flow<Response<Double>>
}

