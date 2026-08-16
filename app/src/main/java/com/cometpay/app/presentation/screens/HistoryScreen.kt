package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun HistoryScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    val ctx = LocalContext.current
    val txns = remember { Store.history(ctx) }
    ScreenHeader("Transactions", onBack)
    if (txns.isEmpty()) {
        Note("No transactions yet. Your payments will show up here.")
    } else {
        Rows(txns.map { Triple(it.title, it.sub, it.amount) }) { Amount(it) }
    }
}
