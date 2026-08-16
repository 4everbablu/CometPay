package com.cometpay.ussd

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.cometpay.UssdFrameListener
import com.cometpay.UssdReaderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Scripted *99# flow; steps match against prompt text in order.
sealed class FlowSpec(
    val code: String,
    val timeoutMs: Long,
    val moneyMovement: Boolean = false,
    val successTitle: String = "Done",
    val failureTitle: String = "Could not complete",
) {
    abstract val steps: List<Step>

    data class Step(
        val match: Regex,
        val replyVar: String? = null,
        val literalReply: String? = null,
        val done: Boolean = false,
        val label: String,
        val delayMs: Long = 250L,
    )

    private companion object {
        fun re(p: String) = Regex(p, RegexOption.IGNORE_CASE)
    }

    object SendUpi : FlowSpec("*99*1*3#", 30_000L, moneyMovement = true, successTitle = "Payment complete", failureTitle = "Payment failed") {
        override val steps = listOf(
            Step(re("(receiver|payee|recipient|vpa|virtual.*payment|upi.*id)"), replyVar = "vpa", label = "Sending UPI ID"),
            Step(re("\\bamount\\b"), replyVar = "amount", label = "Sending amount"),
            Step(re("\\b(remark|comment|note)\\b"), replyVar = "note", label = "Adding note"),
            Step(re("\\bupi\\s*pin\\b|\\b(enter|6\\s*digit).*pin\\b"), replyVar = "pin", label = "Verifying PIN"),
            Step(re("\\b(confirm|press\\s*1|are you sure)\\b"), literalReply = "1", label = "Confirming"),
            Step(re("successful|payment\\s+(?:sent|completed|done)|thank\\s*you\\s*for\\s*using|reference\\s+(?:no|number|id)\\s*[:\\-]"), done = true, label = "Payment complete"),
        )
    }

    object SendMobile : FlowSpec("*99*1*1#", 30_000L, moneyMovement = true, successTitle = "Payment complete", failureTitle = "Payment failed") {
        override val steps = listOf(
            Step(re("mobile|phone|number"), replyVar = "mobile", label = "Sending mobile number"),
            Step(re("amount"), replyVar = "amount", label = "Sending amount"),
            Step(re("remark|comment|note"), replyVar = "note", label = "Adding note"),
            Step(re("upi\\s*pin|enter.*pin|\\d\\s*digit.*pin"), replyVar = "pin", label = "Verifying PIN"),
            Step(re("successful|reference|transferred"), done = true, label = "Payment complete"),
        )
    }

    object SendBank : FlowSpec("*99*1*4#", 35_000L, moneyMovement = true, successTitle = "Transfer complete", failureTitle = "Transfer failed") {
        override val steps = listOf(
            Step(re("ifsc|bank.*code"), replyVar = "ifsc", label = "Sending IFSC"),
            Step(re("account.*number|beneficiary.*account"), replyVar = "account", label = "Sending account number"),
            Step(re("confirm.*account|re.?enter.*account"), replyVar = "account", label = "Confirming account"),
            Step(re("amount"), replyVar = "amount", label = "Sending amount"),
            Step(re("remark|comment|note"), replyVar = "note", label = "Adding note"),
            Step(re("upi\\s*pin|enter.*pin|\\d\\s*digit.*pin"), replyVar = "pin", label = "Verifying PIN"),
            Step(re("confirm|press\\s*1|are you sure"), literalReply = "1", label = "Confirming"),
            Step(re("successful|reference|transferred"), done = true, label = "Transfer complete"),
        )
    }

    object CheckBalance : FlowSpec("*99*3#", 20_000L, successTitle = "Balance fetched", failureTitle = "Balance check failed") {
        override val steps = listOf(
            Step(re("upi\\s*pin|enter.*pin|\\d\\s*digit.*pin"), replyVar = "pin", label = "Verifying PIN"),
            Step(re("balance|avail(able)?|rs\\.?\\s*\\d|inr\\s*\\d|₹\\s*\\d|amount\\s*(is|:)|ledger|a/c\\s*bal"), done = true, label = "Balance fetched"),
        )
    }

    object Transactions : FlowSpec("*99*6#", 20_000L, successTitle = "Statement fetched", failureTitle = "Could not fetch statement") {
        override val steps = listOf(
            Step(re("upi\\s*pin|enter.*pin|\\d\\s*digit.*pin"), replyVar = "pin", label = "Verifying PIN"),
            Step(re("statement|transaction|debit|credit|dr\\b|cr\\b"), done = true, label = "Statement fetched"),
        )
    }

    object RequestUpi : FlowSpec("*99*2*2#", 30_000L, successTitle = "Request sent", failureTitle = "Request failed") {
        override val steps = listOf(
            Step(re("payer|sender|from|vpa|upi.*id"), replyVar = "vpa", label = "Sending UPI ID"),
            Step(re("amount"), replyVar = "amount", label = "Sending amount"),
            Step(re("remark|comment|note"), replyVar = "note", label = "Adding note"),
            Step(re("confirm|press\\s*1|are you sure"), literalReply = "1", label = "Confirming request"),
            Step(re("successful|request.*sent|reference"), done = true, label = "Request sent"),
        )
    }

    object RequestMobile : FlowSpec("*99*2*1#", 30_000L, successTitle = "Request sent", failureTitle = "Request failed") {
        override val steps = listOf(
            Step(re("mobile|phone|number"), replyVar = "mobile", label = "Sending mobile number"),
            Step(re("amount"), replyVar = "amount", label = "Sending amount"),
            Step(re("remark|comment|note"), replyVar = "note", label = "Adding note"),
            Step(re("successful|request.*sent|reference"), done = true, label = "Request sent"),
        )
    }

    object ChangePin : FlowSpec("*99*7*2#", 30_000L, successTitle = "PIN changed", failureTitle = "PIN change failed") {
        override val steps = listOf(
            Step(re("old|current.*pin"), replyVar = "oldPin", label = "Verifying current PIN"),
            Step(re("new.*pin"), replyVar = "newPin", label = "Setting new PIN"),
            Step(re("confirm|re.?enter.*pin"), replyVar = "newPin", label = "Confirming new PIN"),
            Step(re("successful|pin.*changed|pin.*set"), done = true, label = "PIN changed"),
        )
    }

    object PendingRequests : FlowSpec("*99*5#", 25_000L, successTitle = "Requests fetched", failureTitle = "Could not fetch requests") {
        override val steps = listOf(
            Step(re("pending|request|approve|reject|no.*request"), done = true, label = "Requests fetched"),
        )
    }
}

sealed class EngineEvent {
    data class Progress(val label: String) : EngineEvent()
    data class Success(val text: String) : EngineEvent()
    data class Failure(val error: String, val title: String? = null) : EngineEvent()
    data class Pending(val message: String) : EngineEvent()
}

enum class PermissionKind { CALL, OVERLAY, ACCESSIBILITY }

// Runs one *99# session: show overlay, dial, match prompts, send replies, report result.
class UssdEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val overlay = OverlayController(context)

    @Volatile private var sessionActive = false
    private var slowWatchJob: Job? = null
    private var hardTimeoutJob: Job? = null
    private var lastDialTime = 0L

    private var vars: Map<String, String> = emptyMap()
    private var flow: FlowSpec? = null
    private var stepIndex = -1
    private var authorizationSubmitted = false

    var onEvent: (EngineEvent) -> Unit = {}

    init { overlay.onCancel = { cancelByUser() } }

    fun isBusy() = sessionActive

    fun missingPermissions(): List<PermissionKind> {
        val out = mutableListOf<PermissionKind>()
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) out += PermissionKind.CALL
        if (!Settings.canDrawOverlays(context)) out += PermissionKind.OVERLAY
        if (UssdReaderService.instance == null) out += PermissionKind.ACCESSIBILITY
        return out
    }

    fun startPayment(vpa: String, amount: String, note: String, pin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.SendUpi, mapOf("vpa" to vpa, "amount" to amount, "note" to note.ifBlank { "UPI payment" }, "pin" to pin),
            "Paying ₹$amount", "To $vpa — hang tight while the bank confirms.", simSlot)

    fun startMobilePayment(mobile: String, amount: String, note: String, pin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.SendMobile, mapOf("mobile" to mobile, "amount" to amount, "note" to note.ifBlank { "UPI payment" }, "pin" to pin),
            "Paying ₹$amount", "To mobile $mobile", simSlot)

    fun startBankPayment(account: String, ifsc: String, amount: String, note: String, pin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.SendBank, mapOf("account" to account, "ifsc" to ifsc, "amount" to amount, "note" to note.ifBlank { "Bank transfer" }, "pin" to pin),
            "Paying ₹$amount", "To account ending ${account.takeLast(4)}", simSlot)

    fun startBalanceCheck(pin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.CheckBalance, mapOf("pin" to pin), "Fetching balance", "Contacting your bank over *99#…", simSlot)

    fun startTransactions(pin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.Transactions, mapOf("pin" to pin), "Fetching transactions", "Contacting your bank over *99#", simSlot)

    fun startMoneyRequest(vpa: String, amount: String, note: String, simSlot: Int = 0) {
        val mobile = vpa.filter(Char::isDigit)
        val requestFlow = if (mobile.length == 10 && !vpa.contains('@')) FlowSpec.RequestMobile else FlowSpec.RequestUpi
        startFlow(requestFlow, mapOf("vpa" to vpa, "mobile" to mobile, "amount" to amount, "note" to note.ifBlank { "Payment request" }),
            "Requesting ₹$amount", "From $vpa", simSlot)
    }

    fun startPinChange(oldPin: String, newPin: String, simSlot: Int = 0) =
        startFlow(FlowSpec.ChangePin, mapOf("oldPin" to oldPin, "newPin" to newPin), "Changing UPI PIN", "Contacting your bank securely", simSlot)

    fun startPendingRequests(simSlot: Int = 0) =
        startFlow(FlowSpec.PendingRequests, emptyMap(), "Fetching pending requests", "Contacting your bank over *99#", simSlot)

    fun cancelByUser() {
        if (!sessionActive) return
        sessionActive = false
        cleanup()
        overlay.showResult(false, "Cancelled", "The banking session was cancelled.")
        onEvent(EngineEvent.Failure("Cancelled", "Cancelled"))
    }

    fun hideOverlay() {
        scope.launch {
            repeat(4) { UssdReaderService.instance?.dismissDialog(); delay(150L) }
            overlay.hide()
        }
    }

    // ── core flow ────────────────────────────────────────────────────────────

    private fun startFlow(flow: FlowSpec, vars: Map<String, String>, startTitle: String, startSubtitle: String, simSlot: Int) {
        // every early return below must emit an event
        val now = SystemClock.elapsedRealtime()
        if (now - lastDialTime < DOUBLE_TAP_COOLDOWN_MS) {
            onEvent(EngineEvent.Failure("Give the last request a moment to finish, then try again.")); return
        }
        val missing = missingPermissions()
        if (missing.isNotEmpty()) { onEvent(EngineEvent.Failure(describeMissing(missing))); return }

        val missingVar = flow.steps.firstOrNull { it.replyVar != null && vars[it.replyVar].isNullOrBlank() }?.replyVar
        if (missingVar != null) { onEvent(EngineEvent.Failure("Missing $missingVar for this request.")); return }

        lastDialTime = now
        UssdReaderService.instance?.dismissDialog()
        UssdReaderService.instance?.resetForNewSession()
        cleanup()

        sessionActive = true
        UssdReaderService.instance?.sessionActive = true
        this.vars = vars
        this.flow = flow
        this.stepIndex = -1
        this.authorizationSubmitted = false

        // show overlay first, else the carrier dialog flashes
        overlay.show(startTitle, startSubtitle, showSpinner = true)
        UssdReaderService.instance?.frameListener = frameListener

        startSlowWatch()
        startHardTimeout(flow.timeoutMs)

        val encoded = flow.code.replace("#", Uri.encode("#"))
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$encoded")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            phoneAccountForSlot(simSlot)?.let { putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it) }
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            terminate("Could not open the dialer.")
        }
    }

    private fun describeMissing(missing: List<PermissionKind>) = when {
        PermissionKind.ACCESSIBILITY in missing -> "Accessibility service is disabled. Enable Comet Pay under Settings › Accessibility."
        PermissionKind.CALL in missing -> "Phone permission is required to dial *99#."
        PermissionKind.OVERLAY in missing -> "Display-over-other-apps is required to hide the carrier screen."
        else -> "Grant Phone, Accessibility, and Overlay in Settings first."
    }

    private val frameListener = object : UssdFrameListener {
        override fun onFrame(text: String, isMenu: Boolean, isTerminal: Boolean) {
            if (!sessionActive) return
            val currentFlow = flow ?: return
            resetSlowWatch()

            if (UNIVERSAL_SUCCESS.any { it.containsMatchIn(text) }) { succeed(currentFlow, currentFlow.successTitle, text); return }
            if (FAILURE_PATTERNS.any { it.containsMatchIn(text) }) { fail(currentFlow.failureTitle, text); return }

            val matchedIndex = ((stepIndex + 1) until currentFlow.steps.size)
                .firstOrNull { currentFlow.steps[it].match.containsMatchIn(text) }

            if (matchedIndex != null) {
                val step = currentFlow.steps[matchedIndex]
                stepIndex = matchedIndex
                onEvent(EngineEvent.Progress(step.label))
                overlay.updateStep(step.label)
                if (step.done) { succeed(currentFlow, step.label, text); return }

                val reply = step.literalReply ?: step.replyVar?.let { vars[it] }
                if (reply.isNullOrBlank()) { terminate("The bank asked for something Comet Pay does not have."); return }
                if (UssdReaderService.instance == null) { terminate("Automation service stopped — re-enable it in Settings."); return }

                scope.launch {
                    delay(step.delayMs)
                    if (!sessionActive) return@launch
                    val ok = UssdReaderService.instance?.sendReply(reply) == true
                    if (ok) {
                        if (step.replyVar == "pin") authorizationSubmitted = true
                        resetSlowWatch()
                    }
                }
                return
            }

            // no step matched and no input field: an unexpected terminal screen
            if (isTerminal) {
                if (currentFlow.moneyMovement && authorizationSubmitted) {
                    val message = text.ifBlank { "The bank did not return a final status." }
                    closeSession()
                    overlay.showResult(null, "Status pending", message)
                    onEvent(EngineEvent.Pending(message))
                } else {
                    fail(currentFlow.failureTitle, text)
                }
            }
        }

        override fun onSessionEnded(reason: String) {
            if (!sessionActive) return
            terminateUncertain("Session ended before the bank returned a final status.")
        }
    }

    // close the session before emitting
    private fun succeed(flow: FlowSpec, title: String, text: String) {
        closeSession(); overlay.showResult(true, title, text); onEvent(EngineEvent.Success(text))
    }

    private fun fail(title: String, text: String) {
        closeSession(); overlay.showResult(false, title, text); onEvent(EngineEvent.Failure(text, title))
    }

    // overlay stays up; the caller hides it on the result screen
    private fun closeSession() { sessionActive = false; cleanup() }

    private fun terminate(error: String) {
        if (!sessionActive) return
        val title = flow?.failureTitle ?: "Could not complete"
        sessionActive = false
        cleanup()
        overlay.showResult(false, title, error)
        onEvent(EngineEvent.Failure(error, title))
    }

    // money moved + PIN submitted → Pending, otherwise Failure
    private fun terminateUncertain(message: String) {
        if (!sessionActive) return
        if (flow?.moneyMovement == true && authorizationSubmitted) {
            sessionActive = false
            cleanup()
            overlay.showResult(null, "Status pending", message)
            onEvent(EngineEvent.Pending(message))
        } else terminate(message)
    }

    private fun cleanup() {
        slowWatchJob?.cancel(); slowWatchJob = null
        hardTimeoutJob?.cancel(); hardTimeoutJob = null
        UssdReaderService.instance?.sessionActive = false
        UssdReaderService.instance?.dismissDialog()
        vars = emptyMap(); flow = null; stepIndex = -1; authorizationSubmitted = false
    }

    private fun phoneAccountForSlot(slot: Int): PhoneAccountHandle? {
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return null
        return try {
            val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecom?.callCapablePhoneAccounts.orEmpty().getOrNull(slot)
        } catch (_: SecurityException) { null }
    }

    private fun startSlowWatch() {
        slowWatchJob = scope.launch {
            delay(SLOW_WATCH_MS)
            if (sessionActive) terminateUncertain(
                if (flow?.moneyMovement == true) "The bank has not returned a final payment status."
                else "Your bank stopped responding over *99#. Try again in a moment.")
        }
    }

    private fun resetSlowWatch() {
        if (!sessionActive) return
        slowWatchJob?.cancel(); startSlowWatch()
    }

    private fun startHardTimeout(ms: Long) {
        hardTimeoutJob = scope.launch {
            delay(ms)
            if (sessionActive) terminateUncertain(
                if (flow?.moneyMovement == true) "The payment session timed out before confirmation."
                else "The *99# session timed out before your bank replied.")
        }
    }

    companion object {
        private const val DOUBLE_TAP_COOLDOWN_MS = 2_000L
        private const val SLOW_WATCH_MS = 12_000L

        val UNIVERSAL_SUCCESS = listOf(
            Regex("\\bis\\s+successful\\b", RegexOption.IGNORE_CASE),
            Regex("successfully\\s+(?:sent|paid|completed|debited|transferred)", RegexOption.IGNORE_CASE),
            Regex("transaction\\s+successful|txn\\s+successful|payment\\s+successful", RegexOption.IGNORE_CASE),
            Regex("payment\\s+(?:to\\s+\\S+\\s+)?(?:for\\s+)?(?:rs\\.?|inr|₹)?\\s*\\d.*successful", RegexOption.IGNORE_CASE),
            Regex("ref(?:erence)?\\s*(?:id|no|number|#)?\\s*[:\\-]?\\s*\\d{6,}", RegexOption.IGNORE_CASE),
        )

        val FAILURE_PATTERNS = listOf(
            Regex("\\bnot\\s+(a\\s+)?(valid|registered|recognised|recognized|allowed|found|active|enabled|known|supported)\\b", RegexOption.IGNORE_CASE),
            Regex("\\bnot\\s+(debited|sent|processed|completed)\\b", RegexOption.IGNORE_CASE),
            Regex("transaction\\s+(declined|failed|cancelled|denied|aborted)", RegexOption.IGNORE_CASE),
            Regex("unable\\s+to\\s+process", RegexOption.IGNORE_CASE),
            Regex("\\b(invalid|incorrect|wrong|bad)\\s+(pin|upi|vpa|amount|input|entry|id|password|number|account)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(pin|upi\\s*pin|upi\\s*id|vpa|password)\\s+(is\\s+)?(incorrect|invalid|wrong|mismatch)\\b", RegexOption.IGNORE_CASE),
            Regex("entered\\s+(upi\\s+)?pin\\s+(is\\s+)?(incorrect|invalid|wrong)", RegexOption.IGNORE_CASE),
            Regex("enter\\s+(a\\s+)?(valid|correct)", RegexOption.IGNORE_CASE),
            Regex("please\\s+enter\\s+(correct|valid)", RegexOption.IGNORE_CASE),
            Regex("please\\s+check\\s+and\\s+try\\s+again", RegexOption.IGNORE_CASE),
            Regex("psp\\s+(is\\s+)?not\\s+(registered|recognised|recognized)", RegexOption.IGNORE_CASE),
            Regex("vpa\\s+(does\\s+not\\s+exist|is\\s+not\\s+(registered|valid))", RegexOption.IGNORE_CASE),
            Regex("upi\\s*id\\s+(is\\s+)?(invalid|incorrect|wrong)", RegexOption.IGNORE_CASE),
            Regex("(no|not\\s+a)\\s+(account|user|customer)\\s+(found|registered|exists)", RegexOption.IGNORE_CASE),
            Regex("account\\s+(not\\s+found|does\\s+not\\s+exist)", RegexOption.IGNORE_CASE),
            Regex("user\\s+not\\s+found", RegexOption.IGNORE_CASE),
            Regex("\\bsame\\s+(account|vpa|user)\\b", RegexOption.IGNORE_CASE),
            Regex("cannot\\s+(send|pay)\\s+to\\s+(self|yourself|same)", RegexOption.IGNORE_CASE),
            Regex("insufficient\\s+(funds|balance)", RegexOption.IGNORE_CASE),
            Regex("exceed(s|ed)?\\s+(limit|amount)|over\\s+limit", RegexOption.IGNORE_CASE),
            Regex("service\\s+(unavailable|not\\s+available|down)", RegexOption.IGNORE_CASE),
            Regex("try\\s+again\\s+later|temporarily\\s+unavailable", RegexOption.IGNORE_CASE),
            Regex("session\\s+(timed\\s+out|expired|terminated)", RegexOption.IGNORE_CASE),
            Regex("could\\s+not\\s+find\\s+(your|ur)\\s+bank", RegexOption.IGNORE_CASE),
            Regex("is\\s+not\\s+a\\s+valid\\s+selection", RegexOption.IGNORE_CASE),
            Regex("bank\\s+not\\s+found|no\\s+bank\\s+(linked|found)", RegexOption.IGNORE_CASE),
            Regex("pin\\s*(does\\s*not|doesn['']?t)\\s*match|pin\\s*mismatch", RegexOption.IGNORE_CASE),
            Regex("authentication\\s*failed", RegexOption.IGNORE_CASE),
            Regex("max(imum)?\\s*(attempts|tries|retries)", RegexOption.IGNORE_CASE),
            Regex("pin\\s*(blocked|locked|expired)", RegexOption.IGNORE_CASE),
        )
    }
}
