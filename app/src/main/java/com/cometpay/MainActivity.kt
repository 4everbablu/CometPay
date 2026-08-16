package com.cometpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF000000)
private val CardBg = Color(0xFF121212)
private val Line = Color(0xFF262626)
private val IconBg = Color(0xFF1C1C1C)
private val Muted = Color(0xFF8E8E8E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(0), SystemBarStyle.dark(0))
        setContent { App() }
    }
}

@Composable
fun App() = MaterialTheme(darkColorScheme(background = Bg, surface = Bg)) { HomeScreen() }

@Composable
fun HomeScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            Column {
                HorizontalDivider(color = Line)
                NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                    listOf("Home" to Icons.Filled.Home, "Settings" to Icons.Outlined.Settings)
                        .forEachIndexed { i, (label, icon) ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i },
                                icon = { Icon(icon, null, Modifier.size(26.dp)) },
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
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                CometLogo()
                Spacer(Modifier.width(10.dp))
                Text("comet pay", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = {},
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

            Spacer(Modifier.height(44.dp))
            Text("Hello!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Welcome to Comet Pay", color = Muted, fontSize = 15.sp)
            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ActionCard("Scan QR Code", "Scan and pay instantly") { Glyph(Icons.Filled.QrCode) }
                ActionCard("Pay via Contact", "Pay using saved contacts") { Glyph(Icons.Filled.Person) }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ActionCard("Pay via UPI ID", "Enter UPI ID and pay") { UpiLogo() }
                ActionCard("Check Balance", "View your account balance") { Glyph(Icons.Outlined.AccountBalanceWallet) }
            }
        }
    }
}

@Composable
private fun RowScope.ActionCard(title: String, subtitle: String, icon: @Composable () -> Unit) = Surface(
    onClick = {},
    modifier = Modifier.weight(1f).aspectRatio(0.8f),
    shape = RoundedCornerShape(20.dp),
    color = CardBg,
    border = BorderStroke(1.dp, Line),
) {
    Column(Modifier.padding(14.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Box(Modifier.size(84.dp).background(IconBg, CircleShape), Alignment.Center) { icon() }
        Spacer(Modifier.height(22.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(subtitle, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
    }
}

@Composable
private fun Glyph(icon: ImageVector) = Icon(icon, null, Modifier.size(42.dp), Color.White)

// Comet logo kone me
@Composable
private fun CometLogo() = Canvas(Modifier.size(30.dp)) {
    val w = size.width
    drawLine(Color.White, Offset(w * .34f, w * .66f), Offset(w, 0f), w * .1f, StrokeCap.Round)
    drawCircle(Color.White, w * .18f, Offset(w * .2f, w * .8f))
}

// UPI wala watermark 
@Composable
private fun UpiLogo() = Text(
    "UPI",
    color = Color.White,
    fontSize = 24.sp,
    fontWeight = FontWeight.Black,
    fontStyle = FontStyle.Italic,
)

@Preview(showSystemUi = true)
@Composable
private fun HomePreview() = App()
