package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun BankTransferScreen(pad: PaddingValues, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.SendBank
    val confirm = remember { mutableStateOf("") }
    ScreenHeader("Bank Transfer", onBack)
    Field("Account Number", "Enter account number", KeyboardType.Number, state = pay.account)
    Field("Confirm Account Number", "Re-enter account number", KeyboardType.Number, state = confirm)
    Field("IFSC Code", "e.g. SBIN0000123", state = pay.ifsc)
    Field("Amount", "₹ 0", KeyboardType.Decimal, state = pay.amount)
    Gap(6)
    // dono account same aur baaki bhare
    val ready = pay.account.value.isNotBlank() && pay.account.value == confirm.value &&
        pay.ifsc.value.isNotBlank() && pay.amount.value.isNotBlank()
    Submit("Continue", ready) { onOpen("confirm") }
}
