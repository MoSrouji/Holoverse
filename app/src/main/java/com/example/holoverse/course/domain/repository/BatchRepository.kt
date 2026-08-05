package com.example.holoverse.course.domain.repository

import com.example.holoverse.core.utils.Response
import com.example.holoverse.course.domain.Batch
import com.example.holoverse.course.domain.BatchSession
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun createBatch(batch: Batch): Flow<Response<String>>
    fun getBatchesForCourse(courseId: String): Flow<Response<List<Batch>>>
    fun joinBatch(batchId: String, userId: String): Flow<Response<Boolean>>
    
    fun joinOrCreateBatch(
        courseId: String,
        mentorId: String,
        userId: String,
        timeSlot: String
    ): Flow<Response<String>>
    
    fun proposeSessionTimes(sessionId: String, times: List<Timestamp>): Flow<Response<Boolean>>
    fun voteForSessionTime(sessionId: String, userId: String, timeIndex: Int): Flow<Response<Boolean>>
    fun finalizeSessionTime(sessionId: String, selectedTime: Timestamp): Flow<Response<Boolean>>
    
    fun getBatchSessions(batchId: String): Flow<Response<List<BatchSession>>>
    fun getSessionById(sessionId: String): Flow<Response<BatchSession?>>
}
