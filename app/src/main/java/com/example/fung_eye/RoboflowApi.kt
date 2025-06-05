package com.example.fung_eye // Your package name

//import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
//import retrofit2.http.Query
//import retrofit2.http.Url

// Data class for Roboflow Response-
data class RoboflowResponse(
    val predictions: List<Prediction>
)

data class Prediction(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val confidence: Double,
    val class_id: Int,
    val `class`: String
) {
    val className: String
        get() = `class`
}


//interface RoboflowApiService {
//    @Multipart
//    @POST // The URL will be dynamic, so we use @Url
//    suspend fun uploadImage(
//        @Url url: String, // Full URL for the endpoint
//        @Query("api_key") apiKey: String,
//        // Add other query parameters your Roboflow model might need, e.g., confidence, overlap
//        @Query("confidence") confidenceThreshold: Int = 40, // Example: only predictions with 40% confidence
//        @Query("overlap") overlapThreshold: Int = 30,    // Example: 30% overlap for NMS
//        @Part image: MultipartBody.Part
//    ): RoboflowResponse
//}
//
//object RoboflowApi {
//    private const val BASE_URL = "https://detect.roboflow.com/" // Base part of the Roboflow URL
//
//    val instance: RoboflowApiService by lazy {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
//        }
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            // You might need to adjust timeouts for large image uploads
//            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
//            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
//            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
//            .build()
//
//        Retrofit.Builder()
//            .baseUrl(BASE_URL) // Base URL is used here
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(RoboflowApiService::class.java)
//    }
//
//    // Method in ViewModel to call the service:
//    // RoboflowApi.instance.uploadImage(
//    //     url = "${BASE_URL}${ROBOFLOW_MODEL_ENDPOINT}", // Construct the full URL here
//    //     apiKey = ROBOFLOW_API_KEY,
//    //     imageFile = body
//    // )
//}