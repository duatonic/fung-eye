package com.example.fung_eye

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- Data Classes for the Workflow JSON Request Body ---

data class WorkflowRequest(
    val api_key: String,
    val inputs: WorkflowInput
)

data class WorkflowInput(
    val image: ImageInputValue
)

data class ImageInputValue(
    val type: String,
    val value: String // Base64 encoded image string
)


// --- Data Classes for the Workflow JSON Response ---
data class RoboflowFullResponse(
    // The field name "outputs" must match the JSON key. Its value is a List.
    val outputs: List<OutputItem>
)
data class OutputItem(
    // This object contains a single key "predictions" which holds the main payload.
    val predictions: PredictionPayload?
)

data class PredictionPayload(
    @SerializedName("inference_id")
    val inferenceId: String?,
    val time: Double?,
    val image: ImageDetails?,

    // The 'predictions' object has dynamic keys (mushroom names), so we use a Map.
    val predictions: Map<String, ClassificationDetails>?
)

data class ClassificationDetails(
    val confidence: Double,
    @SerializedName("class_id")
    val classId: Int
)

data class ImageDetails(
    val width: Int,
    val height: Int
)

// --- Retrofit API Service and Instance ---

interface RoboflowApiService {
    @POST("infer/workflows/{workspace_name}/{workflow_id}")
    suspend fun analyzeWorkflow(
        @Path("workspace_name") workspaceName: String,
        @Path("workflow_id") workflowId: String,
        @Body request: WorkflowRequest
    ): RoboflowFullResponse
}

object RoboflowApi {
    private const val BASE_URL = "https://serverless.roboflow.com/"

    val instance: RoboflowApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoboflowApiService::class.java)
    }
}