package com.example.fung_eye

import com.example.fung_eye.fungmate.ChatResultResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Data model for the image identification request body.
 * Contains the Base64 encoded image string.
 */
data class IdentifierRequestModel(val image: String)

/**
 * Data model for the response after starting the identification job.
 * Contains the unique job ID.
 */
data class StartIdentifierResponse(val jobId: String)

/**
 * A distinct API service interface for image identification to avoid naming conflicts.
 */
interface ImageIdentifierApiService {
    /**
     * Starts the asynchronous image identification process on the backend.
     */
    @POST("/what-is-this-mushroom")
    suspend fun startIdentifierProcess(@Body requestBody: IdentifierRequestModel): StartIdentifierResponse

    /**
     * Polls for the result of any job (image or chat). The response structure is the same.
     * We can reuse the ChatResultResponse data class from the fungmate package.
     */
    @GET("/chat-result/{jobId}")
    suspend fun getJobResult(@Path("jobId") jobId: String): ChatResultResponse
}

/**
 * A distinct Retrofit client object for the image identification service.
 */
object ImageIdentifierApiClient {
    // private const val BASE_URL = "http://10.0.2.2:5000"
    private const val BASE_URL = "http://192.168.1.17:5000"

    // Custom OkHttpClient to set reasonable timeouts.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ImageIdentifierApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ImageIdentifierApiService::class.java)
    }
}