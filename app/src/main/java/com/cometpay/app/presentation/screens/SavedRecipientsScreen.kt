package com.cometpay.app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SavedRecipientsScreen(pad: PaddingValues, pay: Pay, onBack: () -> Unit, onOpen: (String) -> Unit) = ScreenBody(pad) {
    val ctx = LocalContext.current
    val list = remember { mutableStateListOf<Pair<String, String>>().apply { addAll(Store.recipients(ctx)) } }
    val name = remember { mutableStateOf("") }
    val vpa = remember { mutableStateOf("") }

    ScreenHeader("Saved recipients", onBack)
    if (list.isEmpty()) Note("No saved recipients yet. Add one below.")
    list.forEach { (n, v) ->
        ListTile(n, v, {
            // tap = us recipient ko pay
            pay.flow = Flow.SendUpi; pay.payee.value = v; pay.amount.value = ""; onOpen("upi")
        }, { Initial(n) }) {
            Icon(Icons.Outlined.Close, null, Modifier.size(20.dp).clickable {
                Store.removeRecipient(ctx, v); list.remove(n to v)
            }, Muted)
        }
        Gap(10)
    }

    Gap(10)
    Field("Name", "Recipient name", state = name)
    Field("UPI ID", "name@bank", state = vpa)
    val ready = name.value.isNotBlank() && vpa.value.contains('@')
    Submit("Add recipient", ready) {
        Store.addRecipient(ctx, name.value, vpa.value); list.add(name.value to vpa.value)
        name.value = ""; vpa.value = ""
    }
}
