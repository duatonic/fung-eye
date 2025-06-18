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


@Composable
fun MainScreen(
    onNavigateToIdentify: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToKatalog: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        // --- PERUBAHAN: Teruskan navigasi yang relevan ke setiap komponen ---
        bottomBar = {
            AppBottomNavigation(
                isVisible = isVisible,
                onNavigateToKatalog = onNavigateToKatalog,
                onNavigateToIdentify = onNavigateToIdentify,
                onNavigateToChatbot = onNavigateToChatbot
            )
        }
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
                // Teruskan onNavigateToSettings ke TopImageCard
                TopImageCard(onNavigateToSettings = onNavigateToSettings)
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = spring(stiffness = 50f))
            ) {
                DescriptionBox()
            }

            // Box yang di tengah layar sudah dihapus
        }
    }
}


// --- PERUBAHAN: Tambahkan kembali parameter onNavigateToSettings ---
@Composable
fun TopImageCard(onNavigateToSettings: () -> Unit) {
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
                // --- PERUBAHAN: Kembalikan tombol Settings di sini ---
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
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


// --- PERUBAHAN: AppBottomNavigation hanya untuk 3 tombol utama ---
@Composable
fun AppBottomNavigation(
    isVisible: Boolean,
    onNavigateToKatalog: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToIdentify: () -> Unit
) {
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
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiga tombol aksi utama
                BottomNavItem(icon = Icons.Default.MenuBook, text = "Katalog", onClick = onNavigateToKatalog)
                BottomNavItem(icon = Icons.Default.CameraAlt, text = "Scan", onClick = onNavigateToIdentify)
                BottomNavItem(iconPainter = R.drawable.fungimatelogo, text = "ChatBot", onClick = onNavigateToChatbot)

            }
        }
    }
}

// Composable BottomNavItem tidak perlu diubah
@Composable
fun BottomNavItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconPainter: Int? = null,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (iconPainter != null) {
            Image(
                painter = painterResource(id = iconPainter),
                contentDescription = text,
                modifier = Modifier.size(28.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

// Composable ActionButtonsRow dan ActionButton sudah tidak terpakai lagi
// Anda bisa menghapusnya dari file ini