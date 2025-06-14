package com.example.fung_eye

import android.Manifest
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fung_eye.ui.theme.FungEyeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// --- ADD SETTINGS SCREEN TO NAVIGATION ---
sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object Identify : Screen()
    object Settings : Screen()
    object Katalog : Screen()
}

class MainActivity : ComponentActivity() {
    // --- HANYA GUNAKAN DEKLARASI INI ---
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Langsung gunakan settingsViewModel dari Class
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

            FungEyeTheme(darkTheme = isDarkTheme) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    currentScreen = Screen.Main
                }

                when (currentScreen) {
                    is Screen.Splash -> SplashScreen()

                    // --- BLOK INI YANG DIPERBAIKI ---
                    is Screen.Main -> MainScreen(
                        onNavigateToIdentify = { currentScreen = Screen.Identify },
                        onNavigateToChatbot = {
                            val intent = Intent(this, ChatbotActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onNavigateToKatalog = { currentScreen = Screen.Katalog }
                    )
                    // --- SELESAI PERBAIKAN ---

                    is Screen.Katalog -> KatalogScreen(onNavigateBack = { currentScreen = Screen.Main })

                    is Screen.Identify -> FungEyeApp(
                        onNavigateHome = { currentScreen = Screen.Main }
                    )

                    is Screen.Settings -> SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { currentScreen = Screen.Main }
                    )

                    is Screen.Identify -> FungEyeApp(
                        onNavigateHome = { currentScreen = Screen.Main }
                    )

                    // Tetap teruskan instance ViewModel dari Class
                    is Screen.Settings -> SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onNavigateBack = { currentScreen = Screen.Main }
                    )
                }
            }
        }
    }


@Composable
fun SplashScreen() {
    FungEyeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "FungEye Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FungEye",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
fun FungEyeApp(
    fungEyeViewModel: FungEyeViewModel = viewModel(),
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val analysisResult by fungEyeViewModel.analysisResult.collectAsState()
    val isLoading by fungEyeViewModel.isLoading.collectAsState()

    var showCameraPreview by remember { mutableStateOf(false) }

    val outputDirectory: File = remember {
        val mediaDir = context.filesDir.resolve("fung_eye_camerax_images")
        mediaDir.mkdirs()
        mediaDir
    }

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
                    title = { Text("🍄 Identifikasi Jamur", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateHome) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp)
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Pilih gambar jamur", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (cameraPermissionState.allPermissionsGranted) {
                                        showCameraPreview = true
                                    } else {
                                        cameraPermissionState.launchMultiplePermissionRequest()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo")
                            Spacer(Modifier.width(8.dp))
                            Text("Ambil Foto")
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
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "From Gallery")
                            Spacer(Modifier.width(8.dp))
                            Text("Dari Galeri")
                        }
                    }
                }


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
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Menganalisa...")
                        } else {
                            Text("Periksa Jamur", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (analysisResult.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hasil Analisa:",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = analysisResult,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (analysisResult.contains("beracun", ignoreCase = true))
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Disclaimer: Aplikasi Fung-Eye hanya untuk tujuan informasi. Jangan menjadikan aplikasi ini sebagai satu-satunya acuan untuk identifikasi kelayakan makan jamur. Kesalahan dalam mengidentifikasi bisa berakibat fatal. Selalu berkonsultasilah dengan pakar di bidangnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical=16.dp)
                    )
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

    LaunchedEffect(Unit) {
        try {
            cameraProvider = context.getCameraProvider()
        } catch (e: Exception) {
            Log.e("CameraView", "Error getting camera provider", e)
            onError(ImageCaptureException(ImageCapture.ERROR_CAMERA_CLOSED, "Failed to get camera provider", e))
        }
    }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseCamera) {
                Icon(Icons.Filled.Close, contentDescription = "Close Camera", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            }) {
                Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }

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

fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "TEMP_JPEG_${timeStamp}_"
    val storageDir: File? = context.cacheDir
    if (storageDir != null && !storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
}
}
