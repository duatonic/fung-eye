package com.example.fung_eye

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// Data class untuk merepresentasikan satu pesan chat
data class ChatMessage(
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
    // Mendapatkan konteks untuk tombol kembali
    val context = LocalContext.current

    // Contoh data percakapan, bisa diganti dengan data dinamis nanti
    val chatMessages = listOf(
        ChatMessage("Hello FungiMate, how are you today?", true),
        ChatMessage("Hello! I'm feeling fresh as a forest after rain. How can I assist you with fungi today?", false),
        ChatMessage("How do I identify a harmful mushroom?", true),
        ChatMessage("Harmful mushrooms often have distinct features like bright colors or unusual smells, but identification can be tricky. You can use the fungeye scan tool to get help, or would you like me to analyze a photo for you?", false),
        ChatMessage("Oh, I'll try fungeye then", true),
        ChatMessage("sure, ask again if there is any problem", false),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.fungimatelogo), // <-- Change this name
                            contentDescription = "FungiMate Logo",
                            modifier = Modifier.size(28.dp) // Adjust size as needed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FungiMate", // Nama chatbot
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Online",
                                fontSize = 12.sp,
                                color = Color(0xFF34A853) // Warna hijau untuk status online
                            )
                        }
                    }
                },
                navigationIcon = {
                    // Tombol kembali
                    IconButton(onClick = {
                        (context as? Activity)?.finish() // Menutup activity saat ini dan kembali
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
            // Bagian untuk mengetik pesan
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Write your message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice Message")
                },
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Aksi kirim pesan */ }) {
                        Icon(Icons.Filled.Send, contentDescription = "Send Message", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        // Daftar percakapan yang bisa di-scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            reverseLayout = true // Pesan baru muncul dari bawah
        ) {
            // Membalik daftar agar pesan terakhir ada di bawah
            items(chatMessages.reversed()) { chat ->
                MessageBubble(chatMessage = chat)
            }
        }
    }
}

@Composable
fun MessageBubble(chatMessage: ChatMessage) {
    // Menentukan bubble chat ada di kanan (pengguna) atau kiri (bot)
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
                modifier = Modifier.widthIn(max = 250.dp) // Batasi lebar bubble
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