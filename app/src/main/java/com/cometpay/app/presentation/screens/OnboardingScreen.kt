package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// pehli baar app kholne par: permission -> bank -> sim
@Composable
fun OnboardingScreen(s: Setup, onDone: () -> Unit) = Scaffold(containerColor = Bg) { pad ->
    var step by remember { mutableIntStateOf(0) }
    ScreenBody(pad) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp), Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(Modifier.weight(1f).height(4.dp).background(if (i <= step) Color.White else Line, CircleShape))
            }
        }
        Gap(28)
        when (step) {
            0 -> {
                CometLogo()
                Gap(16)
                Head("Allow permissions", "Comet Pay needs these to run UPI over the carrier menu.")
                Surface(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    CardBg,
                    border = BorderStroke(1.dp, Line),
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) { Perms.forEach { PermRow(it) } }
                }
            }
            1 -> {
                Head("Select bank account", "Linked to your registered mobile number.")
                BankPicker(s)
            }
            else -> {
                Head("Registered SIM", "Your UPI request is sent from this number.")
                Sims.forEachIndexed { i, (n, num) -> Pick(Icons.Outlined.SimCard, n, num, i == s.sim) { s.sim = i } }
            }
        }
        Gap(24)
        Action(if (step == 2) "Start using Comet Pay" else "Continue") { if (step == 2) onDone() else step++ }
        if (step > 0) {
            Gap(10)
            Ghost("Back") { step-- }
        }
    }
}

@Composable
private fun Head(title: String, sub: String) {
    Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Gap(8)
    Note(sub)
    Gap(22)
}
