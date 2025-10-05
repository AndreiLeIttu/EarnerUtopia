package com.aospi.earnerutopia.ui.camera

import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aospi.earnerutopia.network.ApiClient
import com.aospi.earnerutopia.network.Barray
import com.aospi.earnerutopia.network.OptimizeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
private fun captureToCacheAndReturnBytes(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onBytes: (ByteArray) -> Unit,
    scope: CoroutineScope
) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val file = File.createTempFile("camx_$name", ".jpg", context.cacheDir)
    val output = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        output, executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                runCatching { if (file.exists()) file.delete() }
            }
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bytes = runCatching { file.readBytes() }.getOrNull()
                runCatching { if (file.exists()) file.delete() }
                if (bytes != null) onBytes(bytes)
                if (bytes == null) return
//                scope.launch {
//                    try{
//                        val response = ApiClient.apiService.checkDrowsy(bytes)
//                        if (response.isSuccessful) {
//                            val result = response.body()
//                            //TODO view mdoel
//                        }
//                    }  catch (e: Exception) {Log.d("Exception", e.message?:"")}
//                }
            }
        }
    )
}
@Composable
fun CameraXCaptureScreen(
    modifier: Modifier = Modifier,
    onPhotoBytes: (ByteArray) -> Unit,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val scope = rememberCoroutineScope()

    var hasCamPermission by remember { mutableStateOf(false) }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPermission = granted }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestPermission.launch(android.Manifest.permission.CAMERA)
        else hasCamPermission = true
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Column(modifier.fillMaxSize()) {
        if (hasCamPermission) {
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, selector, preview, capture
                    )
                    previewView
                }
            )

            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        captureToCacheAndReturnBytes(
                            context = context,
                            imageCapture = capture,
                            executor = mainExecutor,
                            onBytes = { bytes ->
                                onPhotoBytes(bytes)

                                // Launch API call safely
                                scope.launch {
                                    try {
                                        Log.d("Sendind","Sending request")
                                        val response = ApiClient.apiService.checkDrowsy(Barray(bytes))
                                        if (response.isSuccessful) {
                                            Log.d("CameraX", "API response: ${response.body()}")
                                            Log.d("camerax", ""+(response.body()==true))
                                        } else {
                                            Log.d("CameraX", "API failed: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CameraX", "Exception: ${e.message}")
                                    }
                                    onDone()
                                }
                            },
                            scope = scope
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) { Text("Capture") }

        } else {
            Spacer(Modifier.weight(1f))
            Text("Camera permission required", modifier = Modifier.padding(16.dp))
        }
    }
}
