package com.example.fung_eye

import android.content.pm.ApplicationInfo
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fung_eye.ImageInputValue
import com.example.fung_eye.RoboflowApi
import com.example.fung_eye.WorkflowInput
import com.example.fung_eye.WorkflowRequest
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun analyzeImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = ""
            Log.d("FungEyeViewModel", "Retrofit: Analyzing image: ${imageFile.absolutePath}")

            try {
                // Read the image file and encode it to a Base64 string
                val imageBytes = imageFile.readBytes()
                // NO_WRAP to prevent newlines in the Base64 string
                val encodedFile = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

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

                Log.d("FungEyeViewModel", "Roboflow Workflow Parsed Response Output: ${fullResponse.outputs[0].output}")
                Log.d("FungEyeViewModel", "Roboflow Workflow Parsed Response Detection Prediction: ${fullResponse.outputs[0].detection_predictions?.predictions}")

                val responseItem = fullResponse.outputs.getOrNull(0)
                if (responseItem == null) {
                    _analysisResult.value = "Workflow response contained no outputs."
                    _isLoading.value = false
                    return@launch
                }

                val mainPrediction = responseItem.output?.predictions?.maxByOrNull { it.confidence }
                var resultText = ""

                // Access the list 'outputs' from the fullResponse
                if (mainPrediction != null) {
                    val className = mainPrediction.className
                    val confidence = mainPrediction.confidence
                    // val species = speciesPrediction.className

                    if (confidence < 0.70f) {
                        _analysisResult.value = "Gambar bukan merupakan jamur"
                        return@launch
                    }

                    resultText += "Terdeteksi: $className\n(Confidence: ${String.format("%.1f", confidence * 100)}%)"

                    if (className.contains("beracun", ignoreCase = true) && !className.contains("tidak", ignoreCase = true)) {
                        resultText += "\nStatus: Kemungkinan Besar Beracun"
                    } else if (className.contains("tidak beracun", ignoreCase = true) || className.contains("edible", ignoreCase = true)) {
                        resultText += "\nStatus: Kemungkinan Besar Tidak Beracun"
                    } else {
                        resultText += "\nStatus: Kelayakan untuk dimakan Tidak Diketahui"
                    }
                    // _analysisResult.value = resultText
                } else {
                    _analysisResult.value =
                        "Tidak ada prediksi valid yang ditemukan dalam respons workflow."
                }

                val speciesPrediction = responseItem.detection_predictions?.predictions?.predictions?.maxByOrNull { it.confidence }

                if (speciesPrediction != null) {
                    // Append the species name to the final result text!
                    resultText += "\nSpesies Jamur: ${speciesPrediction.className}"
                }
                else {
                    resultText += "\nSpesies Jamur: tidak terdeteksi"
                }

                _analysisResult.value = resultText

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