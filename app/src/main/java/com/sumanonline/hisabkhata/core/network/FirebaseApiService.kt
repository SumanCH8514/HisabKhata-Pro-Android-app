package com.sumanonline.hisabkhata.core.network

import com.sumanonline.hisabkhata.core.network.model.FirebaseAuthResponse
import com.sumanonline.hisabkhata.core.network.model.FirebaseAuthSignInRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseAuthSignUpRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseCustomerDto
import com.sumanonline.hisabkhata.core.network.model.FirebasePasswordResetRequest
import com.sumanonline.hisabkhata.core.network.model.FirebasePasswordResetResponse
import com.sumanonline.hisabkhata.core.network.model.FirebaseSignInWithIdpRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseSignInWithIdpResponse
import com.sumanonline.hisabkhata.core.network.model.FirebaseTokenRefreshResponse
import com.sumanonline.hisabkhata.core.network.model.FirebaseTransactionDto
import com.sumanonline.hisabkhata.core.network.model.FirebaseUserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FirebaseApiService {

    @POST("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword")
    suspend fun signInWithPassword(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthSignInRequest
    ): Response<FirebaseAuthResponse>

    @POST("https://identitytoolkit.googleapis.com/v1/accounts:signUp")
    suspend fun signUpWithPassword(
        @Query("key") apiKey: String,
        @Body request: FirebaseAuthSignUpRequest
    ): Response<FirebaseAuthResponse>

    @POST("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode")
    suspend fun sendPasswordReset(
        @Query("key") apiKey: String,
        @Body request: FirebasePasswordResetRequest
    ): Response<FirebasePasswordResetResponse>

    @POST("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp")
    suspend fun signInWithIdp(
        @Query("key") apiKey: String,
        @Body request: FirebaseSignInWithIdpRequest
    ): Response<FirebaseSignInWithIdpResponse>

    @POST("https://securetoken.googleapis.com/v1/token")
    @FormUrlEncoded
    suspend fun refreshIdToken(
        @Query("key") apiKey: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<FirebaseTokenRefreshResponse>

    @GET("customers.json")
    suspend fun getCustomersByUser(
        @Query("orderBy") orderBy: String = "\"userId\"",
        @Query("equalTo") equalTo: String,
        @Query("auth") auth: String
    ): Response<Map<String, FirebaseCustomerDto>?>

    @GET("customers/{id}.json")
    suspend fun getCustomerById(
        @Path("id") customerId: String,
        @Query("auth") auth: String
    ): Response<FirebaseCustomerDto?>

    @PUT("customers/{id}.json")
    suspend fun putCustomer(
        @Path("id") customerId: String,
        @Body customer: FirebaseCustomerDto,
        @Query("auth") auth: String
    ): Response<FirebaseCustomerDto>

    @PATCH("customers/{id}.json")
    suspend fun patchCustomer(
        @Path("id") customerId: String,
        @Body updates: kotlinx.serialization.json.JsonObject,
        @Query("auth") auth: String
    ): Response<Unit>

    @DELETE("customers/{id}.json")
    suspend fun deleteCustomer(
        @Path("id") customerId: String,
        @Query("auth") auth: String
    ): Response<Unit>

    @GET("transactions.json")
    suspend fun getTransactionsByUser(
        @Query("orderBy") orderBy: String = "\"userId\"",
        @Query("equalTo") equalTo: String,
        @Query("auth") auth: String
    ): Response<Map<String, FirebaseTransactionDto>?>

    @GET("transactions.json")
    suspend fun getTransactionsByCustomer(
        @Query("orderBy") orderBy: String = "\"customerId\"",
        @Query("equalTo") equalTo: String,
        @Query("auth") auth: String
    ): Response<Map<String, FirebaseTransactionDto>?>

    @PUT("transactions/{id}.json")
    suspend fun putTransaction(
        @Path("id") transactionId: String,
        @Body transaction: FirebaseTransactionDto,
        @Query("auth") auth: String
    ): Response<FirebaseTransactionDto>

    @DELETE("transactions/{id}.json")
    suspend fun deleteTransaction(
        @Path("id") transactionId: String,
        @Query("auth") auth: String
    ): Response<Unit>

    @GET("users/{userId}.json")
    suspend fun getUserProfile(
        @Path("userId") userId: String,
        @Query("auth") auth: String
    ): Response<FirebaseUserDto?>

    @PATCH("users/{userId}.json")
    suspend fun patchUserProfile(
        @Path("userId") userId: String,
        @Body updates: kotlinx.serialization.json.JsonObject,
        @Query("auth") auth: String
    ): Response<Unit>
}
