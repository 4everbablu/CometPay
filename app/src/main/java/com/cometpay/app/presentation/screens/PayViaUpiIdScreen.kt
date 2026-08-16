package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PayViaUpiIdScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Pay via UPI ID", onBack)
    Field("UPI ID", "test@upi")
    Field("Amount", "₹ 0")
    Field("Note (optional)", "-")
    Spacer(Modifier.height(6.dp))
    Action("Verify and Pay")
}
