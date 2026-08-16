package com.cometpay

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.cometpay.app.presentation.screens.*

class MainActivity : FragmentActivity() {
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
    val ctx = LocalContext.current
    // onboarding sirf pehli baar, lock on ho to pehle unlock
    var onboarded by remember { mutableStateOf(Store.onboarded(ctx)) }
    var locked by remember { mutableStateOf(Store.screenLock(ctx)) }
    val setup = remember { Setup(ctx.applicationContext) }
    when {
        locked -> LockScreen { locked = false }
        onboarded -> Main(setup)
        else -> OnboardingScreen(setup) { Store.setOnboarded(ctx); onboarded = true }
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    val activity = LocalContext.current as FragmentActivity
    LaunchedEffect(Unit) { promptUnlock(activity, onUnlock) }
    Box(Modifier.fillMaxSize().background(Bg), Alignment.Center) {
        Column(Modifier.padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Lock, null, Modifier.size(72.dp), Muted)
            Gap(18)
            Text("Comet Pay is locked", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Gap(28)
            Action("Unlock") { promptUnlock(activity, onUnlock) }
        }
    }
}

@Composable
private fun Main(s: Setup) {
    // stack ka last = current screen, first = kaun sa tab
    var stack by remember { mutableStateOf(listOf("home")) }
    val pay = remember { Pay() }
    val back = { stack = if (stack.size > 1) stack.dropLast(1) else listOf("home") }
    val go: (String) -> Unit = { stack = stack + it }
    val home = { stack = listOf("home") }
    BackHandler(stack != listOf("home")) { back() }

    Scaffold(
        containerColor = Bg,
        bottomBar = { BottomNav(stack.first()) { stack = listOf(it) } },
    ) { pad ->
        when (stack.last()) {
            "settings" -> SettingsTab(pad, s, go)
            "scan" -> ScanQrScreen(pad, pay, back, go)
            "bank" -> BankTransferScreen(pad, pay, back, go)
            "contact" -> PayViaContactScreen(pad, pay, back, go)
            "upi" -> PayViaUpiIdScreen(pad, pay, back, go)
            "request" -> RequestMoneyScreen(pad, s, pay, back, go)
            "history" -> HistoryScreen(pad, back)
            "pending" -> PendingRequestsScreen(pad, s, pay, back, go)
            "pin" -> ChangeUpiPinScreen(pad, s, pay, back, go)
            "saved" -> SavedRecipientsScreen(pad, pay, back, go)
            "banks" -> BankAccountsScreen(pad, s, back)
            "sim" -> SimScreen(pad, s, back)
            "confirm" -> PaymentConfirmScreen(pad, s, pay, back, go)
            "enterpin" -> UpiPinScreen(pad, s, pay, back, go)
            "done" -> PaymentStatusScreen(pad, pay, home)
            else -> HomeTab(pad, pay, go)
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
