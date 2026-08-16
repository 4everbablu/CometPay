package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaymentConfirmScreen(
    pad: PaddingValues,
    s: Setup,
    pay: Pay,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) = ScreenBody(pad) {
    val payee = if (pay.flow == Flow.SendBank) "A/C ${pay.account.value}" else pay.payee.value
    ScreenHeader("Confirm Payment", onBack)
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), CardBg, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(60.dp).background(IconBg, CircleShape), Alignment.Center) { Initial(payee) }
            Gap(12)
            Note(payee)
            Gap(16)
            Text("₹ ${pay.amount.value}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    }
    Gap(16)
    Item(Icons.Outlined.AccountBalance, "Paying from", "${s.bankName} · ${s.bankAcc}") { Chevron() }
    Item(Icons.AutoMirrored.Outlined.Notes, "Note", pay.note.value.ifBlank { "-" }) {}
    Item(Icons.Outlined.Shield, "Protected by UPI", "PIN is asked in the next step") {}
    Gap(20)
    Action("Proceed to Pay") { onOpen("enterpin") }
    Gap(10)
    Ghost("Cancel", onBack)
}
