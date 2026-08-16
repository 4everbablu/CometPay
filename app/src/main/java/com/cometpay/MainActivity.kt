package com.cometpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometpay.app.presentation.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(0), SystemBarStyle.dark(0))
        setContent { App() }
    }
}

@Composable
fun App() = MaterialTheme(darkColorScheme(background = Bg, surface = Bg)) { Root() }

@Composable
private fun Root() {
    // stack ka last = current screen, first = kaun sa tab
    var stack by remember { mutableStateOf(listOf("home")) }
    val back = { stack = if (stack.size > 1) stack.dropLast(1) else listOf("home") }
    val go: (String) -> Unit = { stack = stack + it }
    BackHandler(stack != listOf("home")) { back() }

    Scaffold(
        containerColor = Bg,
        bottomBar = { BottomNav(stack.first()) { stack = listOf(it) } },
    ) { pad ->
        when (stack.last()) {
            "settings" -> SettingsTab(pad)
            "scan" -> ScanQrScreen(pad, back)
            "bank" -> BankTransferScreen(pad, back)
            "contact" -> PayViaContactScreen(pad, back)
            "upi" -> PayViaUpiIdScreen(pad, back)
            "request" -> RequestMoneyScreen(pad, back)
            "history" -> HistoryScreen(pad, back)
            "pending" -> PendingRequestsScreen(pad, back)
            "pin" -> ChangeUpiPinScreen(pad, back)
            "saved" -> SavedRecipientsScreen(pad, back)
            else -> HomeTab(pad, go)
        }
    }
}

@Composable
private fun BottomNav(tab: String, onNav: (String) -> Unit) = Column {
    HorizontalDivider(color = Line)
    NavigationBar(containerColor = Color(0xFF0A0A0A)) {
        listOf(
            Triple("Home", Icons.Filled.Home, "home"),
            Triple("Scan QR Code", Icons.Filled.QrCode, "scan"),
            Triple("Settings", Icons.Outlined.Settings, "settings"),
        ).forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = tab == route,
                onClick = { onNav(route) },
                icon = { Icon(icon, null, Modifier.size(if (route == "scan") 34.dp else 26.dp)) },
                label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    indicatorColor = IconBg,
                ),
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomePreview() = App()
