package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UpiPinScreen(pad: PaddingValues, s: Setup, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    var pin by remember { mutableStateOf("") }
    var len by remember { mutableIntStateOf(6) }
    val focus = remember { FocusRequester() }
    // ek frame ruk kar focus lo, warna field attach hone se pehle crash ho sakta hai
    LaunchedEffect(Unit) {
        withFrameNanos {}
        focus.requestFocus()
    }
    val utility = pay.flow == Flow.Balance || pay.flow == Flow.Transactions
    val label = if (utility) "Continue" else "Pay ₹ ${pay.amount.value.ifBlank { "0" }}"

    ScreenHeader("Enter UPI PIN", onBack)
    ListTile(s.bankName, s.bankAcc, {}, { TileIcon(Icons.Outlined.AccountBalance) })
    Gap(24)
    // kuch bank 4 digit ka PIN dete hain, kuch 6
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(4, 6).forEach { n ->
            Surface(
                onClick = { len = n; pin = pin.take(n) },
                shape = RoundedCornerShape(10.dp),
                color = if (len == n) Color.White else CardBg,
                border = BorderStroke(1.dp, Line),
            ) {
                Text(
                    "$n-digit",
                    color = if (len == n) Bg else Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }
        }
    }
    Gap(26)
    Text(
        "ENTER $len-DIGIT UPI PIN",
        Modifier.fillMaxWidth(),
        Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Gap(18)
    // asli text chhupa hua hai, upar dots dikhte hain, keyboard mobile ka default
    BasicTextField(
        value = pin,
        onValueChange = { v -> if (v.length <= len && v.all(Char::isDigit)) pin = v },
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
        textStyle = TextStyle(Color.Transparent, 1.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().height(26.dp), Alignment.Center) {
                inner()
                Dots(pin.length, len)
            }
        },
    )
    Gap(30)
    Submit(label, pin.length == len) { runUssd(pay, pin, s.sim); onOpen("done") }
    Gap(16)
    Note("Never share your UPI PIN with anyone.")
}
