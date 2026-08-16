package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun RequestMoneyScreen(pad: PaddingValues, s: Setup, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.Request
    ScreenHeader("Request Money", onBack)
    Field("UPI ID or Mobile", "name@bank or 10 digit number", state = pay.payee)
    Field("Amount", "₹ 0", KeyboardType.Decimal, state = pay.amount)
    Field("Note (optional)", "What is this for?", state = pay.note)
    Gap(6)
    val ready = pay.payee.value.isNotBlank() && pay.amount.value.isNotBlank()
    Submit("Send Request", ready) { runUssd(pay, "", s.sim); onOpen("done") }
}
