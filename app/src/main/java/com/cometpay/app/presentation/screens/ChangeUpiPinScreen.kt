package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.runtime.Composable

@Composable
fun ChangeUpiPinScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Change UPI PIN", onBack)
    ListTile("Test Bank", "XXXX XXXX 0000", {}, { TileIcon(Icons.Outlined.AccountBalance) })
    Gap(20)
    Field("Old UPI PIN", "• • • •")
    Field("New UPI PIN", "• • • •")
    Field("Confirm New PIN", "• • • •")
    Note("Update through your bank. PIN is never stored in the app.")
    Gap()
    Action("Change PIN")
}
