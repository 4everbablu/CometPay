package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
fun PendingRequestsScreen(pad: PaddingValues, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    ScreenHeader("Pending Requests", onBack)
    Note("Incoming requests waiting for your approval.")
    Gap()
    Rows(
        listOf(
            Triple("Request One", "test1@upi", "₹ 0"),
            Triple("Request Two", "test2@upi", "₹ 0"),
        ),
        onClick = { onOpen("confirm") },
    ) { Amount(it) }
}
