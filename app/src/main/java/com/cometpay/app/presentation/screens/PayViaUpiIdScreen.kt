package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PayViaUpiIdScreen(pad: PaddingValues, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.SendUpi
    ScreenHeader("Pay via UPI ID", onBack)
    Field("UPI ID", "name@bank", KeyboardType.Email, state = pay.payee)
    Field("Amount", "₹ 0", KeyboardType.Decimal, state = pay.amount)
    Field("Note (optional)", "What is this for?", state = pay.note)
    Gap(6)
    val ready = pay.payee.value.isNotBlank() && pay.amount.value.isNotBlank()
    Submit("Verify and Pay", ready) { onOpen("confirm") }
}
