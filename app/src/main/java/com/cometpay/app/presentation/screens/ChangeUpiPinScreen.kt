package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ChangeUpiPinScreen(pad: PaddingValues, s: Setup, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Change UPI PIN", onBack)
    ListTile(s.bankName, s.bankAcc, {}, { TileIcon(Icons.Outlined.AccountBalance) })
    Gap(20)
    Field("Old UPI PIN", "Enter old PIN", KeyboardType.NumberPassword, true)
    Field("New UPI PIN", "4 or 6 digits", KeyboardType.NumberPassword, true)
    Field("Confirm New PIN", "Re-enter new PIN", KeyboardType.NumberPassword, true)
    Note("Update through your bank. PIN is never stored in the app.")
    Gap()
    Action("Change PIN")
}
