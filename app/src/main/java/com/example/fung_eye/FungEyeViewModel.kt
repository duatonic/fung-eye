package com.example.fung_eye

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File

class FungEyeViewModel : ViewModel() {
    private val roboflowApiKey = BuildConfig.ROBOFLOW_API_KEY
    private val roboflowWorkspaceName = BuildConfig.ROBOFLOW_WORKSPACE_NAME
    private val roboflowWorkflowId = BuildConfig.ROBOFLOW_WORKFLOW_ID

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

            Log.d("FungEyeViewModel", "Retrofit: Analyzing image: ${imageFile.absolutePath}")

            try {
                // Read the image file and encode it to a Base64 string
                val imageBytes = imageFile.readBytes()
                // NO_WRAP to prevent newlines in the Base64 string
                val encodedFile = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                Log.d("FungEyeViewModel", "Base64 Encoded Image: $encodedFile")

                // Create the JSON request body using the data classes
                val request = WorkflowRequest(
                    api_key = roboflowApiKey,
                    inputs = WorkflowInput(
                        image = ImageInputValue(
                            type = "base64",
                            value = encodedFile
                        )
                    )
                )

                // Call the Retrofit API service
                Log.d("FungEyeViewModel", "Sending Base64 image to Roboflow Workflow...")
                val fullResponse = RoboflowApi.instance.analyzeWorkflow(
                    workspaceName = roboflowWorkspaceName,
                    workflowId = roboflowWorkflowId,
                    request = request
                )
                Log.d("FungEyeViewModel", "Roboflow Workflow Parsed Response: ${fullResponse}")

                val predictionsMap = fullResponse.outputs.getOrNull(0)?.predictions?.predictions
                Log.d("FungEyeViewModel", "Roboflow Workflow Parsed Response Map: ${predictionsMap}")

                if (!predictionsMap.isNullOrEmpty()) {
                    val topPredictionEntry = predictionsMap.maxByOrNull { it.value.confidence }

                    if (topPredictionEntry != null) {
                        val parts = topPredictionEntry.key.split("_")
                        val edibility = parts.last()

                        val nameParts = if (edibility == "edible" || edibility == "poisonous") {
                            parts.dropLast(1) // Drop the last element
                        } else {
                            parts // Keep all parts
                        }

                        val className = nameParts.joinToString(" ")
                        val confidence = topPredictionEntry.value.confidence
                        _predictedClassName.value = className

                        if (confidence < 0.04f) {
                            _analysisResult.value = "Gambar bukan merupakan jamur"
                            return@launch
                        }

//                        var confidence_percentage = confidence * 1000
//                        if (confidence_percentage >= 100f) {
//                            confidence_percentage /= 10
//                        }

                        var resultText = "Terdeteksi Jamur: ${className} (Confidence: ${String.format("%.1f", confidence * 100f)}%)\n"

                        if (edibility == "poisonous") {
                            resultText += "Status: Kemungkinan Besar Beracun"
                        } else if (edibility == "edible") {
                            resultText += "Status: Kemungkinan Besar Tidak Beracun"
                        } else {
                            resultText += "Status: Kelayakan untuk dimakan Tidak Diketahui"
                        }

                        _analysisResult.value = resultText
                    }
                    else {
                        _analysisResult.value = "Tidak ada prediksi valid yang ditemukan dalam respons workflow."
                    }
                }
                else {
                    _analysisResult.value = "Tidak ada respon dari server."
                    _isLoading.value = false
                    return@launch
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