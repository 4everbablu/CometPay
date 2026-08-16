package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
fun PayViaContactScreen(pad: PaddingValues, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    ScreenHeader("Pay via Contact", onBack)
    Field("Search", "Name or mobile number")
    Rows(
        listOf(
            Triple("Contact One", "+91 00000 00001", ""),
            Triple("Contact Two", "+91 00000 00002", ""),
            Triple("Contact Three", "+91 00000 00003", ""),
        ),
        onClick = { onOpen("confirm") },
    ) { Chevron() }
}
