package com.example.fung_eye

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.fung_eye.ui.theme.FungEyeTheme
import kotlinx.coroutines.launch

// Data class untuk merepresentasikan satu pesan chat
data class ChatMessage(
    val id: Long = System.currentTimeMillis(), // Tambahkan ID unik untuk setiap pesan
    val message: String,
    val isFromUser: Boolean
)

class ChatbotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FungEyeTheme {
                ChatbotScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen() {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // --- PERUBAHAN 1: Hapus data hardcode dan mulai dengan daftar kosong ---
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    // Tambahkan pesan sambutan dari bot saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        chatMessages.add(ChatMessage(message = "Halo! Saya FungiMate, asisten jamur Anda. Ada yang bisa saya bantu?", isFromUser = false))
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
                            Text(
                                text = "• Online",
                                fontSize = 12.sp,
                                color = Color(0xFF34A853)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Aksi untuk volume */ }) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Volume")
                    }
                    IconButton(onClick = { /* TODO: Aksi untuk upload */ }) {
                        Icon(Icons.Filled.Upload, contentDescription = "Upload")
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
                onValueChange = { newText ->
                    textState = newText
                },
                placeholder = { Text("Tulis pesan Anda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Mic, contentDescription = "Pesan Suara")
                },
                trailingIcon = {
                    // --- PERUBAHAN 3: Tambahkan logika pengiriman pesan ---
                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                // Tambahkan pesan pengguna ke daftar
                                val userMessage = ChatMessage(message = textState, isFromUser = true)
                                chatMessages.add(userMessage)

                                // Kosongkan input field
                                textState = ""

                                // Otomatis scroll ke pesan terbaru
                                coroutineScope.launch {
                                    listState.animateScrollToItem(chatMessages.size -1)
                                }

                                // TODO: Kirim `userMessage` ke API AI Anda di sini
                                // dan tambahkan responsnya ke `chatMessages`
                            }
                        },
                        enabled = textState.isNotBlank()
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Kirim Pesan",
                            tint = if (textState.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- PERUBAHAN 2: Gunakan ID unik sebagai key dan teruskan listState ---
        LazyColumn(
            state = listState, // Tambahkan state untuk kontrol scroll
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = chatMessages,
                key = { it.id } // Gunakan ID sebagai key untuk performa
            ) { chat ->
                MessageBubble(chatMessage = chat)
            }
        }
    }
}

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


@Preview(showBackground = true)
@Composable
fun ChatbotScreenPreview() {
    FungEyeTheme {
        ChatbotScreen()
    }
}