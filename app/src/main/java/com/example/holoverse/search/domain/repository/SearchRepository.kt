package com.example.holoverse.search.domain.repository

interface SearchRepository {
    suspend fun courseSearch()
    suspend fun mentorSearch()



}