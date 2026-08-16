package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF000000)
val CardBg = Color(0xFF121212)
val Line = Color(0xFF262626)
val IconBg = Color(0xFF1C1C1C)
val Muted = Color(0xFF8E8E8E)
val Credit = Color(0xFF4ADE80)

// testing ke liye
val Banks = listOf(
    "State Bank of India" to "XXXX XXXX 0001",
    "HDFC Bank" to "XXXX XXXX 0002",
    "ICICI Bank" to "XXXX XXXX 0003",
    "Punjab National Bank" to "XXXX XXXX 0004",
    "Axis Bank" to "XXXX XXXX 0005",
    "Bank of Baroda" to "XXXX XXXX 0006",
    "Kotak Mahindra Bank" to "XXXX XXXX 0007",
    "Canara Bank" to "XXXX XXXX 0008",
    "Union Bank of India" to "XXXX XXXX 0009",
    "IndusInd Bank" to "XXXX XXXX 0010",
)
val Sims = listOf("SIM 1 - Test Carrier" to "+91 00000 00001", "SIM 2 - Test Carrier" to "+91 00000 00002")

// bank aur sim ka selection, onboarding aur app dono isi ko padhte hain
class Setup {
    var bank by mutableIntStateOf(0)
    var sim by mutableIntStateOf(0)
    val other = mutableStateOf("")

    val custom get() = bank == Banks.size
    val bankName get() = if (custom) other.value.ifBlank { "Other bank" } else Banks[bank].first
    val bankAcc get() = if (custom) "XXXX XXXX 0000" else Banks[bank].second
    val simName get() = Sims[sim].first
}

@Composable
fun ScreenBody(pad: PaddingValues, content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.padding(pad).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
) {
    content()
    Spacer(Modifier.height(24.dp))
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null) = Row(
    Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    if (onBack != null) {
        Surface(onClick = onBack, shape = CircleShape, color = IconBg) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.padding(9.dp).size(20.dp), Color.White)
        }
        Spacer(Modifier.width(14.dp))
    }
    Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun RowScope.ActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
) = Surface(
    onClick = onClick,
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
fun ListTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {},
) = Surface(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = CardBg,
    border = BorderStroke(1.dp, Line),
) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).background(IconBg, CircleShape), Alignment.Center) { leading() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Muted, fontSize = 13.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        trailing()
    }
}

// teesri value trailing slot me jati hai
@Composable
fun Rows(
    items: List<Triple<String, String, String>>,
    onClick: () -> Unit = {},
    trailing: @Composable (String) -> Unit = {},
) = items.forEach { (title, sub, extra) ->
    ListTile(title, sub, onClick, { Initial(title) }) { trailing(extra) }
    Spacer(Modifier.height(10.dp))
}

// mobile ka default keyboard, apna nahi
@Composable
fun Field(
    label: String,
    hint: String,
    type: KeyboardType = KeyboardType.Text,
    mask: Boolean = false,
    state: MutableState<String> = remember { mutableStateOf("") },
) = Column(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
    Text(label, color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(7.dp))
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg, border = BorderStroke(1.dp, Line)) {
        Input(state, hint, type, mask, Modifier.fillMaxWidth().padding(15.dp))
    }
}

@Composable
fun Input(
    state: MutableState<String>,
    hint: String,
    type: KeyboardType = KeyboardType.Text,
    mask: Boolean = false,
    modifier: Modifier = Modifier,
) = BasicTextField(
    value = state.value,
    onValueChange = { state.value = it },
    modifier = modifier,
    textStyle = TextStyle(Color.White, 16.sp),
    keyboardOptions = KeyboardOptions(keyboardType = type, imeAction = ImeAction.Done),
    singleLine = true,
    visualTransformation = if (mask) PasswordVisualTransformation('•') else VisualTransformation.None,
    cursorBrush = SolidColor(Color.White),
    decorationBox = { inner ->
        Box {
            if (state.value.isEmpty()) Text(hint, color = Muted, fontSize = 16.sp)
            inner()
        }
    },
)

@Composable
fun Action(text: String, onClick: () -> Unit = {}) = Surface(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
) {
    Text(
        text,
        color = Bg,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

@Composable
fun Ghost(text: String, onClick: () -> Unit = {}) = Surface(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = CardBg,
    border = BorderStroke(1.dp, Line),
) {
    Text(
        text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

// select karne wali row, tick lagta hai
@Composable
fun Pick(icon: ImageVector, title: String, sub: String, on: Boolean, onClick: () -> Unit) {
    ListTile(title, sub, onClick, { TileIcon(icon) }) {
        Icon(
            if (on) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            null,
            Modifier.size(22.dp),
            if (on) Credit else Muted,
        )
    }
    Gap(10)
}

// card ke andar wali patli row
@Composable
fun Item(
    icon: ImageVector,
    title: String,
    sub: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) = Row(
    Modifier.fillMaxWidth()
        .let { if (onClick == null) it else it.clickable(onClick = onClick) }
        .padding(vertical = 10.dp),
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

// granted = green, warna grey "Grant"
@Composable
fun GrantChip(granted: Boolean, onClick: () -> Unit) = Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = IconBg,
) {
    Text(
        if (granted) "Granted" else "Grant",
        color = if (granted) Credit else Muted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
fun Dots(filled: Int, total: Int = 6) = Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
    repeat(total) { i ->
        Box(
            Modifier.size(16.dp)
                .background(if (i < filled) Color.White else Color.Transparent, CircleShape)
                .border(1.dp, if (i < filled) Color.White else Line, CircleShape),
        )
    }
}

@Composable
fun Gap(h: Int = 14) = Spacer(Modifier.height(h.dp))

@Composable
fun Note(text: String) = Text(text, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)

@Composable
fun Amount(text: String) = Text(
    text,
    color = if (text.startsWith("+")) Credit else Color.White,
    fontSize = 15.sp,
    fontWeight = FontWeight.Bold,
)

@Composable
fun Chevron() = Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(20.dp), Muted)

@Composable
fun Glyph(icon: ImageVector) = Icon(icon, null, Modifier.size(42.dp), Color.White)

@Composable
fun TileIcon(icon: ImageVector) = Icon(icon, null, Modifier.size(22.dp), Color.White)

@Composable
fun Initial(name: String) = Text(name.take(1), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

// Comet logo kone me
@Composable
fun CometLogo() = Canvas(Modifier.size(30.dp)) {
    val w = size.width
    drawLine(Color.White, Offset(w * .34f, w * .66f), Offset(w, 0f), w * .1f, StrokeCap.Round)
    drawCircle(Color.White, w * .18f, Offset(w * .2f, w * .8f))
}

// UPI wala watermark
@Composable
fun UpiLogo() = Text(
    "UPI",
    color = Color.White,
    fontSize = 24.sp,
    fontWeight = FontWeight.Black,
    fontStyle = FontStyle.Italic,
)
