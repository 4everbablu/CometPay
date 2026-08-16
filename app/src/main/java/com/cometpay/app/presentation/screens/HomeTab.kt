package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ic null = UPI text logo
private data class Tile(val t: String, val s: String, val ic: ImageVector?, val go: String)

private val tiles = listOf(
    Tile("Bank Transfer", "IFSC and Account", Icons.Outlined.AccountBalance, "bank"),
    Tile("Pay via Contact", "Pay using mobile number", Icons.Filled.Person, "contact"),
    Tile("Pay via UPI ID", "Enter UPI ID and pay", null, "upi"),
    Tile("Request Money", "UPI ID or mobile", Icons.Outlined.Payments, "request"),
    Tile("Transactions", "Last five bank", Icons.AutoMirrored.Outlined.ReceiptLong, "txn"),
    Tile("Pending Requests", "View incoming request", Icons.Outlined.PendingActions, "pending"),
    Tile("Change UPI PIN", "Update through your bank", Icons.Outlined.Lock, "pin"),
    Tile("Saved recipients", "Favorites", Icons.Outlined.StarBorder, "saved"),
)

@Composable
fun HomeTab(pad: PaddingValues, pay: Pay, onOpen: (String) -> Unit) = ScreenBody(pad) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        CometLogo()
        Spacer(Modifier.width(10.dp))
        Text("comet pay", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Surface(
            onClick = { onOpen("history") },
            shape = RoundedCornerShape(14.dp),
            color = CardBg,
            border = BorderStroke(1.dp, Line),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.History, null, Modifier.size(20.dp), Color.White)
                Spacer(Modifier.width(8.dp))
                Text("History", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    Gap(30)
    Text("Hello!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Gap(6)
    Text("Welcome to Comet Pay", color = Muted, fontSize = 15.sp)
    Gap(22)
    BalanceCard { pay.flow = Flow.Balance; onOpen("enterpin") }
    Gap(22)

    tiles.chunked(2).forEachIndexed { i, row ->
        if (i > 0) Gap()
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            row.forEach { c ->
                ActionCard(c.t, c.s, {
                    // txn ko pin chahiye, isliye enterpin se jao
                    if (c.go == "txn") { pay.flow = Flow.Transactions; onOpen("enterpin") } else onOpen(c.go)
                }) { if (c.ic == null) UpiLogo() else Glyph(c.ic) }
            }
        }
    }
}

@Composable
private fun BalanceCard(onRefresh: () -> Unit) = Surface(
    onClick = {},
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = CardBg,
    border = BorderStroke(1.dp, Line),
) {
    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Available Balance", color = Muted, fontSize = 14.sp)
            Gap(5)
            Text("Tap refresh to check", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        // *99*3# se balance
        Surface(onClick = onRefresh, shape = CircleShape, color = IconBg) {
            Icon(Icons.Outlined.Refresh, null, Modifier.padding(9.dp).size(20.dp), Color.White)
        }
    }
}
