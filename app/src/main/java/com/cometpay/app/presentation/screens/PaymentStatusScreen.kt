package com.cometpay.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentStatusScreen(pad: PaddingValues, onDone: () -> Unit) = ScreenBody(pad) {
    Column(
        Modifier.fillMaxWidth().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(90.dp).background(IconBg, CircleShape), Alignment.Center) {
            Icon(Icons.Filled.Check, null, Modifier.size(48.dp), Credit)
        }
        Gap(24)
        Text("Payment Successful", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Gap(10)
        Text("₹ 0", color = Credit, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Gap(10)
        Note("to Recipient One · test1@upi")
        Gap(6)
        Note("UPI Ref 000000000000")
    }
    Gap(40)
    Action("Done", onDone)
}
