package com.cometpay.app.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
fun ScanQrScreen(pad: PaddingValues, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    val ctx = LocalContext.current
    var ok by remember { mutableStateOf(isGranted(ctx, P.CAMERA)) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok = it }
    LifecycleResumeEffect(Unit) {
        ok = isGranted(ctx, P.CAMERA)
        onPauseOrDispose {}
    }
    // screen khulte hi camera permission maang lo
    LaunchedEffect(Unit) { if (!ok) ask.launch(Manifest.permission.CAMERA) }

    ScreenHeader("Scan QR Code", onBack)
    Surface(
        Modifier.fillMaxWidth().aspectRatio(1f),
        RoundedCornerShape(24.dp),
        CardBg,
        border = BorderStroke(1.dp, Line),
    ) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Icon(
                if (ok) Icons.Filled.QrCode else Icons.Outlined.NoPhotography,
                null,
                Modifier.size(96.dp),
                Muted,
            )
        }
    }
    Gap(20)
    Note(if (ok) "Point your camera at any UPI QR code to pay." else "Camera permission is needed to scan QR codes.")
    Gap(20)
    if (ok) {
        Action("Upload from Gallery") { onOpen("confirm") }
    } else {
        Action("Allow camera") { ask.launch(Manifest.permission.CAMERA) }
    }
}
