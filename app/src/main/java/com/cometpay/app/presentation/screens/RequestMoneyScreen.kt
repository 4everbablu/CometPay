package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequestMoneyScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Request Money", onBack)
    Field("UPI ID or Mobile", "test@upi")
    Field("Amount", "₹ 0")
    Field("Note (optional)", "-")
    Spacer(Modifier.height(6.dp))
    Action("Send Request")
}
