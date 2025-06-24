package com.example.fung_eye

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.File

class FungEyeViewModel : ViewModel() {
    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult

    private val _predictedClassName = MutableStateFlow<String?>(null)
    val predictedClassName: StateFlow<String?> = _predictedClassName

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun analyzeImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = ""
            _predictedClassName.value = null

            Log.d("FungEyeViewModel", "Starting image analysis with custom backend.")
            Log.d("FungEyeViewModel", "Retrofit: Analyzing image: ${imageFile.absolutePath}")

            try {
                // Read the image file and encode it to a Base64 string
                val imageBytes = imageFile.readBytes()
                // NO_WRAP to prevent newlines in the Base64 string
                val encodedFile = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                Log.d("FungEyeViewModel", "Base64 Encoded Image: $encodedFile")

                // Create the JSON request body using the data classes
                val request = IdentifierRequestModel(image = encodedFile)
                val startResponse = ImageIdentifierApiClient.instance.startIdentifierProcess(request)
                val jobId = startResponse.jobId
                Log.d("FungEyeViewModel", "Started image identification job with ID: $jobId")

                var isJobDone = false
                var attempts = 0
                val maxAttempts = 36

                while (!isJobDone && attempts < maxAttempts) {
                    attempts++
                    delay(5000L) // Wait 5 seconds

                    Log.d("FungEyeViewModel", "Polling for job $jobId, attempt $attempts")
                    val resultResponse = ImageIdentifierApiClient.instance.getJobResult(jobId)

                    if (resultResponse.status.equals("complete", ignoreCase = true)) {
                        isJobDone = true
                        val backendResponse = resultResponse.response
                        Log.d("FungEyeViewModel", "Job complete. Response: $backendResponse")

                        // 4. Parse the backend's custom response
                        if (backendResponse.isNullOrBlank()) {
                            _analysisResult.value = "Server mengembalikan respon kosong."
                        } else if (backendResponse.contains("error_not_a_mushroom_image", ignoreCase = true)) {
                            _analysisResult.value = "Gambar bukan merupakan jamur."
                        } else {
                            // Expected format: "Amanita Muscaria_Poisonous"
                            val parts = backendResponse.split("_")
                            if (parts.size >= 2) {
                                val name = parts.dropLast(1).joinToString(" ").trim()
                                val toxicity = parts.last().trim()

                                _predictedClassName.value = name

                                var resultText = "Terdeteksi Jamur: $name\n"
                                resultText += when {
                                    toxicity.equals("Poisonous", ignoreCase = true) -> "Status: Kemungkinan Besar Beracun"
                                    toxicity.equals("Edible", ignoreCase = true) -> "Status: Kemungkinan Besar Tidak Beracun"
                                    else -> "Status: Kelayakan untuk dimakan Tidak Diketahui"
                                }
                                _analysisResult.value = resultText
                            } else {
                                _analysisResult.value = "Format respons tidak dikenali: $backendResponse"
                            }
                        }
                    }
                }

                if (!isJobDone) {
                    _analysisResult.value = "Analisa gambar timeout. Silakan coba lagi."
                }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("FungEyeViewModel", "API HTTP Error: ${e.message()}, Body: $errorBody")
                _analysisResult.value = "API Error: ${e.message()}. Check logs for details."
            } catch (e: Exception) {
                Log.e("FungEyeViewModel", "Error analyzing image", e)
                _analysisResult.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}