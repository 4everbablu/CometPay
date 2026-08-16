package com.cometpay.app.presentation.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun ScanQrScreen(pad: PaddingValues, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    val ctx = LocalContext.current
    var ok by remember { mutableStateOf(isGranted(ctx, P.CAMERA)) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok = it }
    LifecycleResumeEffect(Unit) {
        ok = isGranted(ctx, P.CAMERA)
        onPauseOrDispose {}
    }
    LaunchedEffect(Unit) { if (!ok) ask.launch(Manifest.permission.CAMERA) }

    ScreenHeader("Scan QR Code", onBack)
    Surface(
        Modifier.fillMaxWidth().aspectRatio(1f),
        RoundedCornerShape(24.dp),
        CardBg,
        border = BorderStroke(1.dp, Line),
    ) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            if (ok) {
                CameraPreview(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) { raw ->
                    // QR se upi id nikaalo, phir pay screen
                    parseUpi(raw)?.let { (vpa, amount) ->
                        pay.flow = Flow.SendUpi
                        pay.payee.value = vpa
                        pay.amount.value = amount
                        onOpen(if (amount.isNotBlank()) "confirm" else "upi")
                    }
                }
            } else {
                Icon(Icons.Outlined.NoPhotography, null, Modifier.size(96.dp), Muted)
            }
        }
    }
    Gap(20)
    Note(if (ok) "Point your camera at any UPI QR code." else "Camera permission is needed to scan QR codes.")
    Gap(20)
    if (!ok) Action("Allow camera") { ask.launch(Manifest.permission.CAMERA) }
}

// upi://pay?pa=vpa&am=amount se (vpa, amount)
private fun parseUpi(raw: String): Pair<String, String>? {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return null
    val vpa = uri.getQueryParameter("pa") ?: return null
    if (vpa.isBlank()) return null
    return vpa to (uri.getQueryParameter("am").orEmpty())
}

@Composable
private fun CameraPreview(modifier: Modifier, onQr: (String) -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val exec = remember { Executors.newSingleThreadExecutor() }
    var handled by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { exec.shutdown() } }

    AndroidView(modifier = modifier, factory = { c ->
        val view = PreviewView(c)
        val future = ProcessCameraProvider.getInstance(c)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            analysis.setAnalyzer(exec) { img ->
                if (!handled) decode(img)?.let { handled = true; view.post { onQr(it) } }
                img.close()
            }
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(c))
        view
    })
}

private val reader = MultiFormatReader().apply {
    setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
}

// camera frame ki luminance se QR text
private fun decode(img: ImageProxy): String? {
    val plane = img.planes[0]
    val bytes = ByteArray(plane.buffer.remaining()).also { plane.buffer.get(it) }
    val source = PlanarYUVLuminanceSource(bytes, plane.rowStride, img.height, 0, 0, img.width, img.height, false)
    return runCatching { reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text }
        .also { reader.reset() }.getOrNull()
}
