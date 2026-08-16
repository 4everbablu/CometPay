package com.cometpay.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsTab(pad: PaddingValues) = ScreenBody(pad) {
    ScreenHeader("Settings")
    Section("Permissions", Icons.Outlined.Security) {
        Perm(Icons.Outlined.Call, "Phone calls", "Used to dial *99# USSD codes", true)
        Perm(Icons.Outlined.Layers, "Display over other apps", "Runs task in background", false)
        Perm(
            Icons.Outlined.Accessibility,
            "Accessibility service",
            "Reads the replies to carrier menus automatically",
            false,
        )
    }
    Gap(12)
    Section("Payment setup", Icons.Outlined.CreditCard) {
        Item(Icons.Outlined.AccountBalance, "Bank accounts", "Test Bank") { Chevron() }
        Item(Icons.Outlined.SimCard, "Registered SIM", "SIM 1 - Test Carrier") { Chevron() }
        Item(Icons.Outlined.Fingerprint, "Screen lock", "Biometric or device credential") { Toggle() }
    }
}

@Composable
private fun Section(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    var open by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (open) 180f else 0f)
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardBg, border = BorderStroke(1.dp, Line)) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { open = !open }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(46.dp).background(IconBg, CircleShape), Alignment.Center) { TileIcon(icon) }
                Spacer(Modifier.width(14.dp))
                Text(
                    title,
                    Modifier.weight(1f),
                    Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(24.dp).rotate(rot), Muted)
            }
            if (open) {
                HorizontalDivider(color = Line)
                Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp), content = content)
            }
        }
    }
}

// granted = green, warna grey "Grant"
@Composable
private fun Perm(icon: ImageVector, title: String, sub: String, granted: Boolean) {
    var ok by remember { mutableStateOf(granted) }
    Item(icon, title, sub) {
        Surface(onClick = { ok = !ok }, shape = RoundedCornerShape(10.dp), color = IconBg) {
            Text(
                if (ok) "Granted" else "Grant",
                color = if (ok) Credit else Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Toggle() {
    var on by remember { mutableStateOf(true) }
    Switch(
        on,
        { on = it },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Bg,
            checkedTrackColor = Credit,
            checkedBorderColor = Credit,
            uncheckedThumbColor = Muted,
            uncheckedTrackColor = IconBg,
            uncheckedBorderColor = Line,
        ),
    )
}

@Composable
private fun Item(icon: ImageVector, title: String, sub: String, trailing: @Composable () -> Unit) = Row(
    Modifier.fillMaxWidth().padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Box(Modifier.size(40.dp).background(IconBg, CircleShape), Alignment.Center) {
        Icon(icon, null, Modifier.size(20.dp), Color.White)
    }
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Gap(3)
        Text(sub, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
    }
    Spacer(Modifier.width(10.dp))
    trailing()
}
