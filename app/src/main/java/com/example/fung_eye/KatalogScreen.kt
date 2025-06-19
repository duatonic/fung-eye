package com.example.fung_eye

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// Data model for Edibility (unchanged)
enum class Edibility(val displayName: String, val color: Color) {
    EDIBLE("Bisa Dimakan", Color(0xFF4CAF50)),
    POISONOUS("Beracun", Color(0xFFF44336)),
    INEDIBLE("Tidak Dimakan", Color.Gray)
}

// Data model for Mushroom (unchanged)
data class Mushroom(
    val id: Int,
    val commonName: String,
    val scientificName: String,
    val imageResId: Int,
    val edibility: Edibility
)

// Dummy data for mushrooms (unchanged)
val dummyMushroomList = listOf(
    Mushroom(1, "Jamur Tiram", "Pleurotus ostreatus", R.drawable.jamur_tiram, Edibility.EDIBLE),
    Mushroom(2, "Jamur Kuping", "Auricularia auricula-judae", R.drawable.jamur_kuping, Edibility.EDIBLE),
    Mushroom(3, "Jamur Merang", "Volvariella volvacea", R.drawable.jamur_merang, Edibility.EDIBLE),
    Mushroom(4, "Amanita Muscaria", "Amanita muscaria", R.drawable.amanita_muscaria, Edibility.POISONOUS),
    Mushroom(5, "Jamur Kancing", "Agaricus bisporus", R.drawable.jamur_kancing, Edibility.EDIBLE),
    Mushroom(6, "Jamur Enoki", "Flammulina velutipes", R.drawable.jamur_enoki, Edibility.EDIBLE)
)

// Data model for Indonesian food dish (moved here)
data class IndonesianFoodDish(
    val id: Int,
    val name: String,
    val description: String,
    val imageResId: Int, // Using drawable ID for image
    val fungiUsed: String // Description of fungi used
)

// Sample data for Indonesian food dishes using fungi (moved here)
val dummyIndonesianFoodList = listOf(
    IndonesianFoodDish(
        id = 1,
        name = "Gudeg Jamur Tiram",
        description = "Gudeg khas Yogyakarta yang dibuat dengan jamur tiram sebagai pengganti nangka muda, dimasak dengan santan dan rempah manis.",
        imageResId = R.drawable.gudeg_jamur_tiram, // You'll need to add this drawable
        fungiUsed = "Jamur Tiram, Jamur Kuping"
    ),
    IndonesianFoodDish(
        id = 2,
        name = "Pepes Jamur Tiram",
        description = "Jamur tiram yang dibumbui rempah khas Indonesia, dibungkus daun pisang dan dikukus.",
        imageResId = R.drawable.pepes_jamur_tiram, // You'll need to add this drawable
        fungiUsed = "Jamur Tiram"
    ),
    IndonesianFoodDish(
        id = 3,
        name = "Sate Jamur",
        description = "Sate yang terbuat dari jamur, seringkali disajikan dengan bumbu kacang.",
        imageResId = R.drawable.sate_jamur, // You'll need to add this drawable
        fungiUsed = "Jamur Tiram, Jamur Kancing"
    ),
    IndonesianFoodDish(
        id = 4,
        name = "Oseng-Oseng Jamur",
        description = "Tumisan sederhana jamur dengan bumbu cabai, bawang, dan kecap, cocok sebagai lauk sehari-hari.",
        imageResId = R.drawable.oseng_oseng_jamur, // You'll need to add this drawable
        fungiUsed = "Jamur Tiram"
    )
)

// Enum to represent the current content type in the catalog
enum class KatalogContentType {
    MUSHROOMS,
    FOODS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogScreen(onNavigateBack: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedContentType by remember { mutableStateOf(KatalogContentType.MUSHROOMS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (selectedContentType) {
                                KatalogContentType.MUSHROOMS -> "Katalog Jamur"
                                KatalogContentType.FOODS -> "Katalog Makanan Jamur"
                            },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Kategori")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Katalog Jamur") },
                                onClick = {
                                    selectedContentType = KatalogContentType.MUSHROOMS
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Katalog Makanan Jamur") },
                                onClick = {
                                    selectedContentType = KatalogContentType.FOODS
                                    expanded = false
                                }
                            )
                        }
                    }
                },
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
            when (selectedContentType) {
                KatalogContentType.MUSHROOMS -> {
                    items(dummyMushroomList) { mushroom ->
                        MushroomCard(mushroom = mushroom)
                    }
                }
                KatalogContentType.FOODS -> {
                    items(dummyIndonesianFoodList) { foodDish ->
                        FoodCard(foodDish = foodDish)
                    }
                }
            }
        }
    }
}

// Composable for a single mushroom item card (unchanged)
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

// Composable for a single food item card (moved here)
@Composable
fun FoodCard(foodDish: IndonesianFoodDish) {
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
                painter = painterResource(id = foodDish.imageResId),
                contentDescription = foodDish.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = foodDish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = foodDish.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jamur yang digunakan: ${foodDish.fungiUsed}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Composable small for edibility status (unchanged)
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