import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fung_eye.fungmate.ChatMessage
import com.example.fung_eye.fungmate.RequestModel
import com.example.fung_eye.fungmate.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    // --- NEW STATE: Tracks if a response is being processed ---
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    init {
        _chatMessages.value = listOf(
            ChatMessage(
                message = "Halo! Saya FungiMate, asisten jamur Anda. Ada yang bisa saya bantu?",
                isFromUser = false
            )
        )
    }

    fun sendMessage(userMessageText: String) {
        // Prevent sending a new message if one is already in progress.
        if (userMessageText.isBlank() || _isProcessing.value) return

        viewModelScope.launch {
            // --- SET STATE TO PROCESSING ---
            _isProcessing.value = true

            // Create a stable ID and a placeholder message for the bot's response
            val botResponseId = UUID.randomUUID().toString()

            try {
                // Add the user's message to the UI immediately
                val userMessage = ChatMessage(message = userMessageText, isFromUser = true)
                _chatMessages.update { it + userMessage }

                val thinkingMessage = ChatMessage(
                    id = botResponseId,
                    message = "FungiMate is thinking...",
                    isFromUser = false,
                    isTyping = true
                )
                _chatMessages.update { it + thinkingMessage }

                // Step 1: Start the process and get a job ID
                val startRequest = RequestModel(userMessage.message)
                val startResponse = RetrofitClient.apiService.startChatProcess(startRequest)
                val jobId = startResponse.jobId

                var isJobDone = false
                var attempts = 0
                val maxAttempts = 36 // 32 attempts * 5s = 180s timeout

                // Step 2: Poll for the result in a loop
                while (!isJobDone && attempts < maxAttempts) {
                    attempts++
                    delay(5000L)

                    val resultResponse = RetrofitClient.apiService.getChatResult(jobId)

                    if (resultResponse.status.equals("complete", ignoreCase = true)) {
                        isJobDone = true
                        val botMessage = ChatMessage(
                            id = botResponseId,
                            message = resultResponse.response ?: "Received an empty response.",
                            isFromUser = false
                        )
                        _chatMessages.update { list ->
                            list.map { if (it.id == botResponseId) botMessage else it }
                        }
                    }
                }

                if (!isJobDone) {
                    throw Exception("Response timed out after $maxAttempts attempts.")
                }

            } catch (e: Exception) {
                // Handle any error
                val errorMessage = ChatMessage(
                    id = botResponseId,
                    message = "Maaf, terjadi kesalahan: ${e.message}",
                    isFromUser = false
                )
                _chatMessages.update { list ->
                    list.map { if (it.id == botResponseId) errorMessage else it }
                }
            } finally {
                // --- RESET STATE ---
                // This block ensures isProcessing is always set back to false,
                // even if an error occurs.
                _isProcessing.value = false
            }
        }
    }
}