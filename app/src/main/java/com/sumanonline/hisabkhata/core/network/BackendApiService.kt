package com.sumanonline.hisabkhata.core.network

import com.sumanonline.hisabkhata.core.network.model.BackendHealthResponse
import com.sumanonline.hisabkhata.core.network.model.EmailRequest
import com.sumanonline.hisabkhata.core.network.model.EmailResponse
import com.sumanonline.hisabkhata.core.network.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BackendApiService {

    @GET("api/health")
    suspend fun checkHealth(): Response<BackendHealthResponse>

    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody,
        @Part("filename") filename: RequestBody? = null
    ): Response<UploadResponse>

    @POST("api/send-email")
    suspend fun sendEmail(
        @Body request: EmailRequest
    ): Response<EmailResponse>
}
