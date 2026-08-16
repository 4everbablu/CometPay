package com.cometpay.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity

@Composable
fun SettingsTab(pad: PaddingValues, s: Setup, onOpen: (String) -> Unit) = ScreenBody(pad) {
    ScreenHeader("Settings")
    Section("Permissions", Icons.Outlined.Security) { Perms.forEach { PermRow(it) } }
    Gap(12)
    Section("Payment setup", Icons.Outlined.CreditCard) {
        Item(Icons.Outlined.AccountBalance, "Bank accounts", s.bankName, { onOpen("banks") }) { Chevron() }
        Item(Icons.Outlined.SimCard, "Registered SIM", s.simName, { onOpen("sim") }) { Chevron() }
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
                Text(title, Modifier.weight(1f), Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(24.dp).rotate(rot), Muted)
            }
            if (open) {
                HorizontalDivider(color = Line)
                Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp), content = content)
            }
        }
    }
}

@Composable
private fun Toggle() {
    val ctx = LocalContext.current
    val activity = ctx as FragmentActivity
    var on by remember { mutableStateOf(Store.screenLock(ctx)) }
    Switch(
        on,
        { want ->
            // on tabhi jab device par biometric/PIN ho
            if (!want || canLock(activity)) { on = want; Store.setScreenLock(ctx, want) }
        },
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
