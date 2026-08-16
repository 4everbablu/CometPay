package com.cometpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF000000)
val CardBg = Color(0xFF121212)
val Line = Color(0xFF262626)
val IconBg = Color(0xFF1C1C1C)
val Muted = Color(0xFF8E8E8E)
val Credit = Color(0xFF4ADE80)

@Composable
fun ScreenBody(pad: PaddingValues, content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
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
fun Rows(items: List<Triple<String, String, String>>, trailing: @Composable (String) -> Unit = {}) =
    items.forEach { (title, sub, extra) ->
        ListTile(title, sub, {}, { Initial(title) }) { trailing(extra) }
        Spacer(Modifier.height(10.dp))
    }

@Composable
fun Field(label: String, value: String) = Column(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
    Text(label, color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(7.dp))
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardBg, border = BorderStroke(1.dp, Line)) {
        Text(value, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(15.dp))
    }
}

@Composable
fun Action(text: String) = Surface(
    onClick = {},
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
