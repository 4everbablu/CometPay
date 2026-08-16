package com.cometpay.app.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
fun PendingRequestsScreen(pad: PaddingValues, s: Setup, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    pay.flow = Flow.Pending
    ScreenHeader("Pending Requests", onBack)
    Note("Fetch incoming collect requests from your bank over *99#.")
    Gap()
    Action("Fetch Pending Requests") { runUssd(pay, "", s.sim); onOpen("done") }
}
