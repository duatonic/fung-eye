package com.example.fung_eye

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fung_eye.ui.theme.FungEyeTheme


// --- LANGKAH 1: PERBARUI DEFINISI FUNGSI MAINSCREEN ---
@Composable
fun MainScreen(
    onNavigateToIdentify: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    FungEyeTheme {
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isVisible = true
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            // --- LANGKAH 2: TERUSKAN onNavigateToSettings KE BOTTOM NAVIGATION ---
            bottomBar = { AppBottomNavigation(isVisible, onNavigateToSettings = onNavigateToSettings) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                ) {
                    TopImageCard()
                }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = spring(stiffness = 50f))
                ) {
                    DescriptionBox()
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ) + fadeIn(animationSpec = spring(stiffness = 50f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer) // Menggunakan warna tema
                            .padding(top = 32.dp)
                    ) {
                        // --- LANGKAH 2: TERUSKAN onNavigateToChatbot KE ACTIONBUTTONSROW ---
                        ActionButtonsRow(onNavigateToIdentify, onNavigateToChatbot)
                    }
                }
            }
        }
    }
}


@Composable
fun TopImageCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.identify_fungi_background),
            contentDescription = "Mushroom background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hallo, Pengguna", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = "Dapatkan informasi seputar Jamur",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Amanita muscaria",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "kingdom: Fungi",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
            }
        }
    }
}

@Composable
fun DescriptionBox() {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Aplikasi ini membantu Anda mengidentifikasi jamur lewat foto, serta menyediakan katalog dan chatbot untuk informasi lebih lanjut.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}


// --- LANGKAH 3: PERBARUI DEFINISI DAN LOGIKA ACTIONBUTTONSROW ---
@Composable
fun ActionButtonsRow(
    onNavigateToIdentify: () -> Unit,
    onNavigateToChatbot: () -> Unit // <-- Parameter sudah ada
) {
    var selectedButton by remember { mutableStateOf("Katalog Jamur") }
    val context = LocalContext.current

    val buttons = listOf(
        Triple(Icons.Default.MenuBook, "Katalog Jamur", "Katalog Jamur"),
        Triple(Icons.Default.Chat, "FungEye\nChatBot", "FungEye ChatBot"),
        Triple(Icons.Default.CameraAlt, "Scan Jamur", "Scan Jamur")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { (icon, text, id) ->
            ActionButton(
                icon = icon,
                text = text,
                isSelected = selectedButton == id,
                onClick = {
                    selectedButton = id
                    when (id) {
                        "Scan Jamur" -> onNavigateToIdentify()
                        "Katalog Jamur" -> Toast.makeText(context, "Membuka Katalog...", Toast.LENGTH_SHORT).show()
                        "FungEye ChatBot" -> onNavigateToChatbot() // <-- Gunakan di sini
                    }
                }
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.95f else 1f

    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val shadowElevation = if (isSelected) 6.dp else 0.dp

    Column(
        modifier = Modifier
            .scale(scale)
            .width(100.dp)
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, color = contentColor, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}


// --- LANGKAH 3: PERBARUI DEFINISI DAN LOGIKA APPBOTTOMNAVIGATION ---
@Composable
fun AppBottomNavigation(isVisible: Boolean, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 50f)
        ) + fadeIn()
    ) {
        BottomAppBar(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Already on home */ }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onNavigateToSettings) { // <-- Gunakan di sini
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}