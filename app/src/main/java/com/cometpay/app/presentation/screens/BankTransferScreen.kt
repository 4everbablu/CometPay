package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BankTransferScreen(pad: PaddingValues, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    ScreenHeader("Bank Transfer", onBack)
    Field("Account Number", "Enter account number", KeyboardType.Number)
    Field("Confirm Account Number", "Re-enter account number", KeyboardType.Number)
    Field("IFSC Code", "e.g. SBIN0000123")
    Field("Amount", "₹ 0", KeyboardType.Decimal)
    Spacer(Modifier.height(6.dp))
    Action("Continue") { onOpen("confirm") }
}
