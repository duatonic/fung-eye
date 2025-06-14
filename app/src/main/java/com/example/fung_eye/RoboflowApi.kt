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
    val outputs: List<WorkflowResponseItem>
)

data class WorkflowResponseItem(
    // main output block containing the final predictions
    val output: OutputData?,

    val detection_predictions: DetectionPredictionsData?
)

data class OutputData(
    val image: ImageDetails?,
    val predictions: List<WorkflowPrediction>?
)

data class DetectionPredictionsData(
    @SerializedName("inference_id")
    val inferenceId: String?,
    val predictions: PredictionsObject?
)

data class PredictionsObject(
    val image: ImageDetails?,
    val predictions: List<WorkflowPrediction>?
)

data class ImageDetails(
    val width: Int?,
    val height: Int?
)

data class WorkflowPrediction(
    val confidence: Double,
    val class_id: Int,
    @SerializedName("class")
    val className: String,
    val x: Double?,
    val y: Double?,
    val width: Double?,
    val height: Double?,
    val detection_id: String?
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