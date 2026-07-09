package com.example.holoverse.threedmodel.data.remote

import com.example.holoverse.threedmodel.data.remote.dto.ModelResponseDto
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getModels(
        @Url url: String = "https://cdn.jsdelivr.net/gh/MoSrouji/3dSample@master/index.json"
    ): ModelResponseDto

    companion object {
        const val BASE_URL = "https://cdn.jsdelivr.net/"
    }
}
