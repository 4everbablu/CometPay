package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun BankAccountsScreen(pad: PaddingValues, s: Setup, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Bank accounts", onBack)
    Note("Accounts linked to your registered mobile number.")
    Gap()
    BankPicker(s)
}

@Composable
fun SimScreen(pad: PaddingValues, s: Setup, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Registered SIM", onBack)
    Note("Your UPI request is sent from this number.")
    Gap()
    Sims.forEachIndexed { i, (name, num) -> Pick(Icons.Outlined.SimCard, name, num, i == s.sim) { s.sim = i } }
}

// 10 bank, aur last me apna bank likhne ka option
@Composable
fun BankPicker(s: Setup) {
    Banks.forEachIndexed { i, (name, acc) ->
        Pick(Icons.Outlined.AccountBalance, name, acc, i == s.bank) { s.bank = i }
    }
    Pick(Icons.Outlined.Edit, "Other bank", s.other.value.ifBlank { "Type your bank name below" }, s.custom) {
        s.bank = Banks.size
    }
    Field("Your bank name", "e.g. Federal Bank", state = s.other)
    // likhna shuru kiya to apne aap select ho jaye
    LaunchedEffect(s.other.value) { if (s.other.value.isNotBlank()) s.bank = Banks.size }
}
