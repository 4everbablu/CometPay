package com.cometpay.app.presentation.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cometpay.CometApp
import com.cometpay.ussd.EngineEvent
import com.cometpay.ussd.UssdEngine

enum class Flow { SendUpi, SendMobile, SendBank, Balance, Transactions, Request, ChangePin, Pending }

// shared input + result for the whole pay flow
class Pay {
    var flow = Flow.SendUpi
    val payee = mutableStateOf("")   // upi id or mobile
    val account = mutableStateOf("")
    val ifsc = mutableStateOf("")
    val amount = mutableStateOf("")
    val note = mutableStateOf("")
    val oldPin = mutableStateOf("")
    val newPin = mutableStateOf("")

    var ok by mutableStateOf<Boolean?>(null)
    var title by mutableStateOf("Processing")
    var message by mutableStateOf("")

    val needsPin get() = flow != Flow.Request && flow != Flow.Pending && flow != Flow.ChangePin
}

fun runUssd(pay: Pay, pin: String, sim: Int) {
    val engine = CometApp.instance.ussdEngine
    pay.ok = null; pay.title = "Processing"; pay.message = ""
    engine.onEvent = { ev ->
        when (ev) {
            is EngineEvent.Progress -> {}
            is EngineEvent.Success -> finish(pay, engine, true, "Successful", ev.text)
            is EngineEvent.Failure -> finish(pay, engine, false, ev.title ?: "Failed", ev.error)
            is EngineEvent.Pending -> finish(pay, engine, null, "Status pending", ev.message)
        }
    }
    when (pay.flow) {
        Flow.SendUpi -> engine.startPayment(pay.payee.value, pay.amount.value, pay.note.value, pin, sim)
        Flow.SendMobile -> engine.startMobilePayment(pay.payee.value, pay.amount.value, pay.note.value, pin, sim)
        Flow.SendBank -> engine.startBankPayment(pay.account.value, pay.ifsc.value, pay.amount.value, pay.note.value, pin, sim)
        Flow.Balance -> engine.startBalanceCheck(pin, sim)
        Flow.Transactions -> engine.startTransactions(pin, sim)
        Flow.ChangePin -> engine.startPinChange(pay.oldPin.value, pay.newPin.value, sim)
        Flow.Request -> engine.startMoneyRequest(pay.payee.value, pay.amount.value, pay.note.value, sim)
        Flow.Pending -> engine.startPendingRequests(sim)
    }
}

private fun finish(pay: Pay, engine: UssdEngine, ok: Boolean?, title: String, message: String) {
    pay.ok = ok; pay.title = title; pay.message = message
    record(pay, ok)
    Handler(Looper.getMainLooper()).postDelayed({ engine.hideOverlay() }, 1500)
}

// only money-moving flows go into history
private fun record(pay: Pay, ok: Boolean?) {
    val ctx = CometApp.instance
    when (pay.flow) {
        Flow.SendUpi, Flow.SendMobile -> Store.addTxn(ctx, pay.payee.value, "-₹ ${pay.amount.value}", ok)
        Flow.SendBank -> Store.addTxn(ctx, "A/C ${pay.account.value}", "-₹ ${pay.amount.value}", ok)
        Flow.Request -> Store.addTxn(ctx, pay.payee.value, "+₹ ${pay.amount.value}", ok)
        else -> {}
    }
}
