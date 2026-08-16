package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ChangeUpiPinScreen(pad: PaddingValues, s: Setup, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.ChangePin
    val confirm = remember { mutableStateOf("") }
    ScreenHeader("Change UPI PIN", onBack)
    ListTile(s.bankName, s.bankAcc, {}, { TileIcon(Icons.Outlined.AccountBalance) })
    Gap(20)
    Field("Old UPI PIN", "Enter old PIN", KeyboardType.NumberPassword, true, pay.oldPin)
    Field("New UPI PIN", "4 or 6 digits", KeyboardType.NumberPassword, true, pay.newPin)
    Field("Confirm New PIN", "Re-enter new PIN", KeyboardType.NumberPassword, true, confirm)
    Note("Update through your bank. PIN is never stored in the app.")
    Gap()
    // naya PIN dono jagah same hona chahiye
    val ready = pay.oldPin.value.isNotBlank() && pay.newPin.value.length >= 4 && pay.newPin.value == confirm.value
    Submit("Change PIN", ready) { runUssd(pay, "", s.sim); onOpen("done") }
}
