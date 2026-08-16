package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PayViaContactScreen(pad: PaddingValues, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.SendMobile
    ScreenHeader("Pay via Contact", onBack)
    Field("Mobile Number", "10 digit number", KeyboardType.Phone, state = pay.payee)
    Field("Amount", "₹ 0", KeyboardType.Decimal, state = pay.amount)
    Field("Note (optional)", "What is this for?", state = pay.note)
    Gap(6)
    val ready = pay.payee.value.length >= 10 && pay.amount.value.isNotBlank()
    Submit("Verify and Pay", ready) { onOpen("confirm") }
}
