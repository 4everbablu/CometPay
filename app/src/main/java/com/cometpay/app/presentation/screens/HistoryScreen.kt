package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
fun HistoryScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Transactions", onBack)
    Note("Last five bank transactions.")
    Gap()
    Rows(
        listOf(
            Triple("Txn One", "UPI · 00:00", "-₹ 0"),
            Triple("Txn Two", "NEFT · 00:00", "+₹ 0"),
            Triple("Txn Three", "UPI · 00:00", "-₹ 0"),
            Triple("Txn Four", "UPI · 00:00", "+₹ 0"),
            Triple("Txn Five", "BBPS · 00:00", "-₹ 0"),
        ),
    ) { Amount(it) }
}
