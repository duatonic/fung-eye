package com.example.fung_eye.fungmate

import ChatViewModel
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fung_eye.R
import com.example.fung_eye.SettingsViewModel
import com.example.fung_eye.ui.theme.FungEyeTheme
import kotlinx.coroutines.launch
import java.util.UUID

// ChatMessage data class
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isFromUser: Boolean,
    val isTyping: Boolean = false
)

// ChatbotActivity class
class ChatbotActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    companion object {
        const val EXTRA_PROMPT_QUERY = "EXTRA_PROMPT_QUERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialQuery = intent.getStringExtra(EXTRA_PROMPT_QUERY)
        chatViewModel.handleInitialPrompt(initialQuery)

        setContent {
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            FungEyeTheme(darkTheme = isDarkTheme) {
                ChatbotScreen(chatViewModel = chatViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Collect both the messages and the new processing state
    val chatMessages by chatViewModel.chatMessages.collectAsState()
    val isProcessing by chatViewModel.isProcessing.collectAsState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.fungimatelogo),
                            contentDescription = "FungiMate Logo",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FungiMate",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = textState,
                onValueChange = { newText -> textState = newText },
                placeholder = { Text(if (isProcessing) "FungiMate is thinking..." else "Tulis pesan Anda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                // Disable the entire text field while processing
                enabled = !isProcessing,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                chatViewModel.sendMessage(textState)
                                textState = ""
                            }
                        },
                        enabled = textState.isNotBlank() && !isProcessing
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Kirim Pesan",
                            tint = if (textState.isNotBlank() && !isProcessing) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = chatMessages,
                key = { it.id }
            ) { chat ->
                MessageBubble(chatMessage = chat)
            }
        }
    }
}

// MessageBubble Composable
@Composable
fun MessageBubble(chatMessage: ChatMessage) {
    val horizontalArrangement = if (chatMessage.isFromUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (chatMessage.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (chatMessage.isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (chatMessage.isFromUser) 20.dp else 0.dp,
                        bottomEnd = if (chatMessage.isFromUser) 0.dp else 20.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = chatMessage.message,
                color = textColor,
                modifier = Modifier.widthIn(max = 250.dp)
            )
        }
    }
}