package com.example.holoverse.three_d_model.data.remote

import com.example.holoverse.three_d_model.data.remote.dto.KhronosAssetDto
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getKhronosModels(
        @Url url: String = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Assets/main/Models/model-index.json"
    ): List<KhronosAssetDto>

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/KhronosGroup/"
    }
}
