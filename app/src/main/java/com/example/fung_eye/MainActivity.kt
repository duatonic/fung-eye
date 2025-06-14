package com.example.fung_eye

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fung_eye.ui.theme.FungEyeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FungEyeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FungEyeApp()
                }
            }
        }
    }
}

// Helper suspend function to get CameraProvider
suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FungEyeApp(fungEyeViewModel: FungEyeViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val analysisResult by fungEyeViewModel.analysisResult.collectAsState()
    val isLoading by fungEyeViewModel.isLoading.collectAsState()

    var showCameraPreview by remember { mutableStateOf(false) }

    // Define output directory for CameraX
    val outputDirectory: File = remember {
        val mediaDir = context.filesDir.resolve("fung_eye_camerax_images")
        mediaDir.mkdirs() // Create directory if it doesn't exist
        mediaDir
    }

    // Permission Handling
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    val galleryPermissionState = rememberMultiplePermissionsState(
        permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    )

    // Activity Result Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            Log.d("FungEyeApp", "Image selected from gallery: $imageUri")
        }
    )

    if (showCameraPreview) {
        CameraView(
            outputDirectory = outputDirectory,
            onImageCaptured = { uri ->
                imageUri = uri
                showCameraPreview = false
                Log.d("FungEyeApp", "CameraX Image captured: $uri")
            },
            onError = { exception ->
                Log.e("FungEyeApp", "CameraX Image capture error", exception)
                showCameraPreview = false
                Toast.makeText(context, "Error capturing image: ${exception.message}", Toast.LENGTH_SHORT).show()
            },
            onCloseCamera = {
                showCameraPreview = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🍄 Fung-Eye", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            // --- PERUBAHAN UTAMA: MENGGUNAKAN LAZYCOLUMN AGAR BISA DI-SCROLL ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp), // Padding horizontal untuk semua item
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Setiap grup elemen dibungkus dalam item()
                item {
                    Spacer(modifier = Modifier.height(16.dp)) // Spacer tambahan di atas
                    Text(
                        "Pendeteksi Jamur",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,

                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected or Captured Mushroom",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("Ambil atau pilih foto jamur", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (cameraPermissionState.allPermissionsGranted) {
                                        showCameraPreview = true // Show CameraX preview
                                    } else {
                                        cameraPermissionState.launchMultiplePermissionRequest()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo", tint = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Ambil Foto", color = MaterialTheme.colorScheme.onSecondary)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (galleryPermissionState.allPermissionsGranted) {
                                        galleryLauncher.launch("image/*")
                                    } else {
                                        galleryPermissionState.launchMultiplePermissionRequest()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "From Gallery", tint = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Ambil Dari Galeri", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }

                // --- KODE BARU DIMASUKKAN DI SINI ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol untuk Katalog Jamur
                        OutlinedButton(
                            onClick = {
                                // TODO: Tambahkan logika untuk membuka halaman Katalog
                                Toast.makeText(context, "Membuka Katalog Jamur...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.MenuBook, // Ikon buku untuk katalog
                                contentDescription = "Katalog Jamur"
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Katalog")
                        }

                        Spacer(Modifier.width(16.dp)) // Jarak antar tombol

                        // Tombol untuk Tingkat Bahaya
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Membuka Chatbot...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.Forum, // Ikon peringatan untuk bahaya
                                contentDescription = "Chatbot"
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Chatbot")
                        }
                    }
                }
                // --- SELESAI KODE BARU ---

                item {
                    Button(
                        onClick = {
                            imageUri?.let { uri ->
                                val imageFileToAnalyze: File? = when (uri.scheme) {
                                    "file" -> File(uri.path ?: "").takeIf { it.exists() }
                                    "content" -> {
                                        val tempFile = createTempImageFile(context)
                                        context.contentResolver.openInputStream(uri)?.use { input ->
                                            tempFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        tempFile.takeIf { it.exists() && it.length() > 0 }
                                    }
                                    else -> null
                                }

                                if (imageFileToAnalyze != null) {
                                    Log.d("FungEyeApp", "Analyzing image file: ${imageFileToAnalyze.absolutePath} (Size: ${imageFileToAnalyze.length()} bytes)")
                                    fungEyeViewModel.analyzeImage(imageFileToAnalyze)
                                } else {
                                    Log.e("FungEyeApp", "Image file not found or URI invalid for analysis: $uri")
                                    Toast.makeText(context, "Invalid image URI", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = imageUri != null && !isLoading,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onTertiary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Analyzing...", color = MaterialTheme.colorScheme.onTertiary)
                        } else {
                            Text("Periksa Jamur", color = MaterialTheme.colorScheme.onTertiary, fontSize = 16.sp)
                        }
                    }
                }

                if (analysisResult.isNotEmpty()) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Hasil Analisa:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Text(
                                text = analysisResult,
                                fontSize = 18.sp,
                                color = if (analysisResult.contains("Poisonous", ignoreCase = true))
                                    MaterialTheme.colorScheme.error
                                else if (analysisResult.contains("Not Poisonous", ignoreCase = true) ||
                                    analysisResult.contains("Likely Not Poisonous", ignoreCase = true))
                                    Color(0xFF006400) // Dark Green
                                else MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                        if (cameraPermissionState.shouldShowRationale || galleryPermissionState.shouldShowRationale) {
                            Text(
                                "Camera and/or Gallery permission is needed to select images. Please grant permissions in app settings if denied.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Disclaimer Text
                        Text(
                            text = "Disclaimer: Aplikasi Fung-Eye hanya untuk tujuan informasi. Jangan menjadikan aplikasi ini sebagai satu-satunya acuan untuk identifikasi kelayakan makan jamur. Kesalahan dalam mengidentifikasi bisa berakibat fatal. Selalu berkonsultasilah dengan pakar di bidangnya.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CameraView(
    outputDirectory: File,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    onCloseCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val previewUseCase = remember { Preview.Builder().build() }
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCaptureUseCase: ImageCapture = remember { ImageCapture.Builder().build() }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    // Get the camera provider once
    LaunchedEffect(Unit) {
        try {
            cameraProvider = context.getCameraProvider()
        } catch (e: Exception) {
            Log.e("CameraView", "Error getting camera provider", e)
            onError(ImageCaptureException(ImageCapture.ERROR_CAMERA_CLOSED, "Failed to get camera provider", e))
        }
    }

    // Rebind use cases when provider or lensFacing changes
    LaunchedEffect(cameraProvider, lensFacing) {
        cameraProvider?.let { provider ->
            try {
                provider.unbindAll()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageCaptureUseCase
                )
                previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
            } catch (exc: Exception) {
                Log.e("CameraView", "Use case binding failed", exc)
                onError(ImageCaptureException(ImageCapture.ERROR_CAMERA_CLOSED, "Use case binding failed", exc))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraProvider != null) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Top controls: Close and Switch Camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.3f), shape = MaterialTheme.shapes.medium)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseCamera) {
                Icon(Icons.Filled.Close, contentDescription = "Close Camera", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Text("Camera Preview", color = Color.White, fontSize = 16.sp)
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            }) {
                Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }

        // Capture Button
        FloatingActionButton(
            onClick = {
                val imageFile = File(
                    outputDirectory,
                    "FUNGEYE_IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

                imageCaptureUseCase.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(imageFile)
                            Log.d("CameraView", "Image saved: $savedUri")
                            onImageCaptured(savedUri)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraView", "Image capture error: ${exception.message}", exception)
                            onError(exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Filled.Camera, "Take photo", modifier = Modifier.size(36.dp))
        }
    }
}

// Helper function to create a temporary image file in app's internal cache. This is used when picking an image from the gallery to copy it before sending to API
fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "TEMP_JPEG_${timeStamp}_"
    // Use cache directory for temporary files
    val storageDir: File? = context.cacheDir
    if (storageDir != null && !storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File.createTempFile(
        imageFileName, // prefix
        ".jpg", // suffix
        storageDir // directory
    )
}