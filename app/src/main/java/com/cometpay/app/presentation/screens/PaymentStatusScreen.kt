package com.cometpay.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentStatusScreen(pad: PaddingValues, pay: Pay, onDone: () -> Unit) = ScreenBody(pad) {
    val icon = when (pay.ok) { true -> Icons.Filled.Check; false -> Icons.Filled.Close; null -> Icons.Outlined.HourglassEmpty }
    val tint = when (pay.ok) { true -> Credit; false -> Color(0xFFFF5A5A); null -> Muted }
    Column(Modifier.fillMaxWidth().padding(top = 70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(90.dp).background(IconBg, CircleShape), Alignment.Center) {
            Icon(icon, null, Modifier.size(48.dp), tint)
        }
        Gap(24)
        Text(pay.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Gap(12)
        Text(pay.message.ifBlank { "Working over *99#…" }, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
    Gap(40)
    Action("Done", onDone)
}
