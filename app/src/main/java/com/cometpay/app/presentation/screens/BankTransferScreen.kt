package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BankTransferScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Bank Transfer", onBack)
    Field("Account Number", "0000 0000 0000")
    Field("Confirm Account Number", "0000 0000 0000")
    Field("IFSC Code", "TEST0000000")
    Field("Amount", "₹ 0")
    Spacer(Modifier.height(6.dp))
    Action("Continue")
}
