package com.example.fung_eye

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fung_eye.ui.theme.FungEyeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    // Apply the theme from the ViewModel to this specific screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp) // Increased spacing
            ) {
                // "Preferensi" Section
                item {
                    SettingsSection(title = "Preferensi") {
                        SettingsItem(
                            icon = Icons.Default.DarkMode,
                            title = "Mode Gelap",
                            control = {
                                Switch(
                                    checked = isDarkTheme,
                                    onCheckedChange = { settingsViewModel.toggleTheme() }
                                )
                            }
                        )
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifikasi",
                            onClick = { Toast.makeText(context, "Navigasi ke Notifikasi", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                // --- "APLIKASI" SECTION ---
                item {
                    SettingsSection(title = "Aplikasi") {
                        SettingsItem(
                            icon = Icons.Default.DeleteSweep,
                            title = "Hapus Cache",
                            onClick = { Toast.makeText(context, "Cache Dihapus", Toast.LENGTH_SHORT).show() }
                        )
                        SettingsItem(
                            icon = Icons.Default.Info, // <-- CORRECTED ICON
                            title = "Tentang Aplikasi",
                            onClick = { Toast.makeText(context, "FungEye v1.0", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }


                // "Lainnya" Section
                item {
                    SettingsSection(title = "Lainnya") {
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            title = "Bantuan",
                            onClick = { Toast.makeText(context, "Navigasi ke Bantuan", Toast.LENGTH_SHORT).show() }
                        )
                        SettingsItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "Kebijakan Privasi",
                            onClick = { Toast.makeText(context, "Navigasi ke Kebijakan Privasi", Toast.LENGTH_SHORT).show() }
                        )
                        SettingsItem(
                            icon = Icons.Default.StarRate,
                            title = "Beri Rating Aplikasi",
                            onClick = { Toast.makeText(context, "Membuka App Store...", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
            }
        }

}


@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    control: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        }
        if (control != null) {
            control()
        } else {
            // Only show the chevron if it's a clickable item without a switch
            if (onClick != null) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
