package com.example.fung_eye

import android.util.Base64 // Android's Base64 for encoding
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson // For parsing JSON response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder // For URL encoding file name
import java.nio.charset.StandardCharsets

class FungEyeViewModel : ViewModel() {

    private val roboflowApiKey = "YOUR_ROBOFLOW_API_KEY"
    private val roboflowModelEndpoint = "YOUR_MODEL_ID/YOUR_VERSION_NUMBER" // e.g., my-mushroom-model/3

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun analyzeImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = ""
            Log.d("FungEyeViewModel", "HttpURLConnection: Analyzing image: ${imageFile.absolutePath}")

            withContext(Dispatchers.IO) { // Perform network operation on IO thread
                var connection: HttpURLConnection? = null
                try {
                    // Read file and Base64 encode
                    val imageBytes = imageFile.readBytes()
                    // NO_WRAP to avoid newlines in the encoded string, which can break parsing.
                    val encodedFile = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                    // Construct URL for Roboflow inference endpoint
                    val encodedFileName = URLEncoder.encode(imageFile.name, StandardCharsets.UTF_8.name())
                    val urlString = "https://detect.roboflow.com/${roboflowModelEndpoint}" +
                            "?api_key=${roboflowApiKey}" +
                            "&name=${encodedFileName}" +
                            "&format=json"

                    val url = URL(urlString)
                    Log.d("FungEyeViewModel", "HttpURLConnection: Connecting to $urlString")

                    // Http Request
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"

                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connection.setRequestProperty("Content-Length", encodedFile.toByteArray(StandardCharsets.UTF_8).size.toString())
                    connection.useCaches = false
                    connection.doOutput = true // Enable writing to the connection output stream (for POST)

                    Log.d("FungEyeViewModel", "HttpURLConnection: Sending Base64 image data...")
                    // Send request
                    DataOutputStream(connection.outputStream).use { wr ->
                        wr.write(encodedFile.toByteArray(StandardCharsets.UTF_8))
                        wr.flush()
                    }

                    // Get Response
                    val responseCode = connection.responseCode
                    val responseMessage = connection.responseMessage // Get the response message
                    Log.d("FungEyeViewModel", "HttpURLConnection: Response Code: $responseCode, Message: $responseMessage")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        // Read the response body as a String
                        val responseBody = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                        Log.d("FungEyeViewModel", "HttpURLConnection: Roboflow Raw Response: $responseBody")

                        // Parse the JSON response using Gson
                        val gson = Gson()
                        val roboflowData = gson.fromJson(responseBody, RoboflowResponse::class.java)

                        // Process roboflowData ---
                        if (roboflowData != null && roboflowData.predictions.isNotEmpty()) {
                            // Sort by confidence
                            val topPrediction = roboflowData.predictions.maxByOrNull { it.confidence }
                            if (topPrediction != null) {
                                var resultText = "Detected: ${topPrediction.className} (Confidence: ${String.format("%.2f", topPrediction.confidence * 100)}%)"
                                // Customize poisonous/edible logic
                                if (topPrediction.className.contains("poisonous", ignoreCase = true)) {
                                    resultText += "\nStatus: Likely Poisonous ☠️"
                                } else if (topPrediction.className.contains("edible", ignoreCase = true) ||
                                    topPrediction.className.contains("non-poisonous", ignoreCase = true) ||
                                    topPrediction.className.contains("safe", ignoreCase = true)) {
                                    resultText += "\nStatus: Likely Not Poisonous ✅"
                                } else {
                                    resultText += "\nStatus: Edibility Unknown 🤔 (${topPrediction.className})"
                                }
                                _analysisResult.value = resultText
                            } else {
                                _analysisResult.value = "No valid predictions found in response."
                            }
                        } else {
                            _analysisResult.value = "No objects detected or prediction list empty."
                        }

                    } else {
                        val errorStream = connection.errorStream
                        val errorBody = errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: "No error body."
                        Log.e("FungEyeViewModel", "HttpURLConnection: API Error Code: $responseCode, Message: $responseMessage, Body: $errorBody")
                        _analysisResult.value = "API Error: $responseCode - $responseMessage. Check logs for details."
                    }

                } catch (e: Exception) {
                    Log.e("FungEyeViewModel", "HttpURLConnection: Exception during image analysis", e)
                    _analysisResult.value = "Error: ${e.message?.take(150)}" // Limit UI error message length
                } finally {
                    connection?.disconnect()
                    _isLoading.value = false
                }
            }
        }
    }
}