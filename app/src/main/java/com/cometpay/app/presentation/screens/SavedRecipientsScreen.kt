package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SavedRecipientsScreen(pad: PaddingValues, onBack: () -> Unit) = ScreenBody(pad) {
    ScreenHeader("Saved recipients", onBack)
    Note("Favorites.")
    Gap()
    Rows(
        listOf(
            Triple("Recipient One", "test1@upi", ""),
            Triple("Recipient Two", "test2@upi", ""),
            Triple("Recipient Three", "test3@upi", ""),
        ),
    ) { Icon(Icons.Filled.Star, null, Modifier.size(20.dp), Credit) }
}
