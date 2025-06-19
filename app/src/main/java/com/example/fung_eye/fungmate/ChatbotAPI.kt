package com.example.fung_eye.fungmate

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// ------------------- Data Models for Polling -------------------

/**
 * Represents the request body sent to the API to start the process.
 * JSON: { "message": "request_message" }
 */
data class RequestModel(val message: String)

/**
 * Represents the immediate response from the server after starting the task.
 * The server should return a unique ID for the job.
 * JSON: { "jobId": "some_unique_id" }
 */
data class StartChatResponse(val jobId: String)

/**
 * Represents the response from the polling endpoint.
 * 'status' can be "pending", "complete", or "failed".
 * 'response' contains the final message only when the status is "complete".
 * JSON: { "status": "pending" | "complete", "response": "optional_message" }
 */
data class ChatResultResponse(val status: String, val response: String?)


// ------------------- Updated Retrofit API Interface -------------------

interface ApiService {
    /**
     * Sends the initial request to start the long-running process.
     * @return A response containing the unique job ID.
     */
    @POST("/start-chat")
    suspend fun startChatProcess(@Body requestBody: RequestModel): StartChatResponse

    /**
     * Polls the server to get the result of the job.
     * @param jobId The unique ID of the job we are checking.
     * @return The current status and potential result of the job.
     */
    @GET("/chat-result/{jobId}")
    suspend fun getChatResult(@Path("jobId") jobId: String): ChatResultResponse
}


// ------------------- Retrofit Client Singleton -------------------

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000"

    // Custom OkHttpClient to set reasonable timeouts.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // Attach the custom client to Retrofit
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}