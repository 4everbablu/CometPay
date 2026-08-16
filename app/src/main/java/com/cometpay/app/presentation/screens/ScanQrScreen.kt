package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScanQrScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Scan QR Code", onBack)
    Surface(
        Modifier.fillMaxWidth().aspectRatio(1f),
        RoundedCornerShape(24.dp),
        CardBg,
        border = BorderStroke(1.dp, Line),
    ) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Icon(Icons.Filled.QrCode, null, Modifier.size(96.dp), Muted)
        }
    }
    Gap(20)
    Note("Point your camera at any UPI QR code to pay.")
    Gap(20)
    Action("Upload from Gallery")
}
