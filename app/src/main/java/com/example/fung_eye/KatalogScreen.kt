package com.example.fung_eye

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fung_eye.ui.theme.FungEyeTheme

// 1. Data model untuk setiap jamur
enum class Edibility(val displayName: String, val color: Color) {
    EDIBLE("Bisa Dimakan", Color(0xFF4CAF50)),
    POISONOUS("Beracun", Color(0xFFF44336)),
    INEDIBLE("Tidak Dimakan", Color.Gray)
}

data class Mushroom(
    val id: Int,
    val commonName: String,
    val scientificName: String,
    val imageResId: Int, // Menggunakan ID dari drawable
    val edibility: Edibility
)

// 2. Contoh data jamur lokal
val dummyMushroomList = listOf(
    Mushroom(1, "Jamur Tiram", "Pleurotus ostreatus", R.drawable.jamur_tiram, Edibility.EDIBLE),
    Mushroom(2, "Jamur Kuping", "Auricularia auricula-judae", R.drawable.jamur_kuping, Edibility.EDIBLE),
    Mushroom(3, "Jamur Merang", "Volvariella volvacea", R.drawable.jamur_merang, Edibility.EDIBLE),
    Mushroom(4, "Amanita Muscaria", "Amanita muscaria", R.drawable.amanita_muscaria, Edibility.POISONOUS),
    Mushroom(5, "Jamur Kancing", "Agaricus bisporus", R.drawable.jamur_kancing, Edibility.EDIBLE),
    Mushroom(6, "Jamur Enoki", "Flammulina velutipes", R.drawable.jamur_enoki, Edibility.EDIBLE)
)
// Catatan: Anda perlu menambahkan gambar jamur_tiram.png, jamur_kuping.png, dll. ke folder res/drawable Anda.


// 3. Composable utama untuk layar katalog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Jamur", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyMushroomList) { mushroom ->
                MushroomCard(mushroom = mushroom)
            }
        }
    }
}

// 4. Composable untuk satu kartu item jamur
@Composable
fun MushroomCard(mushroom: Mushroom) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mushroom.imageResId),
                contentDescription = mushroom.commonName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mushroom.commonName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mushroom.scientificName,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                EdibilityChip(edibility = mushroom.edibility)
            }
        }
    }
}

// Composable kecil untuk status kelayakan makan
@Composable
fun EdibilityChip(edibility: Edibility) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(edibility.color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = edibility.displayName,
            color = edibility.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Preview(showBackground = true)
@Composable
fun KatalogScreenPreview() {
    FungEyeTheme {
        KatalogScreen(onNavigateBack = {})
    }
}