package com.cometpay.app.presentation.screens

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.cometpay.UssdReaderService

enum class P { CAMERA, PHONE, OVERLAY, ACCESS }

class PermDef(val icon: ImageVector, val title: String, val sub: String, val kind: P)

val Perms = listOf(
    PermDef(Icons.Outlined.PhotoCamera, "Camera", "Used to scan UPI QR codes", P.CAMERA),
    PermDef(Icons.Outlined.Call, "Phone calls", "Used to dial *99# USSD codes", P.PHONE),
    PermDef(Icons.Outlined.Layers, "Display over other apps", "Runs task in background", P.OVERLAY),
    PermDef(
        Icons.Outlined.Accessibility,
        "Accessibility service",
        "Reads the replies to carrier menus automatically",
        P.ACCESS,
    ),
)

fun isGranted(ctx: Context, kind: P) = when (kind) {
    P.CAMERA -> has(ctx, Manifest.permission.CAMERA)
    P.PHONE -> has(ctx, Manifest.permission.CALL_PHONE)
    P.OVERLAY -> Settings.canDrawOverlays(ctx)
    P.ACCESS -> ctx.getSystemService(AccessibilityManager::class.java)
        ?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        ?.any { it.resolveInfo.serviceInfo.name == UssdReaderService::class.java.name } == true
}

private fun has(ctx: Context, name: String) =
    ctx.checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED

// asli permission maangta hai, settings se wapas aane par dobara check karta hai
@Composable
fun PermRow(p: PermDef) {
    val ctx = LocalContext.current
    var ok by remember { mutableStateOf(isGranted(ctx, p.kind)) }
    LifecycleResumeEffect(p.kind) {
        ok = isGranted(ctx, p.kind)
        onPauseOrDispose {}
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        ok = isGranted(ctx, p.kind)
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        ok = isGranted(ctx, p.kind)
    }
    Item(p.icon, p.title, p.sub) {
        GrantChip(ok) {
            if (ok) {
                open.launch(appSettings(ctx))
            } else {
                when (p.kind) {
                    P.CAMERA -> ask.launch(Manifest.permission.CAMERA)
                    P.PHONE -> ask.launch(Manifest.permission.CALL_PHONE)
                    P.OVERLAY -> open.launch(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")),
                    )
                    P.ACCESS -> open.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        }
    }
}

private fun appSettings(ctx: Context) =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
