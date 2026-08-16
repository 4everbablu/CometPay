package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequestMoneyScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Request Money", onBack)
    Field("UPI ID or Mobile", "name@bank or 10 digit number")
    Field("Amount", "₹ 0", KeyboardType.Decimal)
    Field("Note (optional)", "What is this for?")
    Spacer(Modifier.height(6.dp))
    Action("Send Request")
}
