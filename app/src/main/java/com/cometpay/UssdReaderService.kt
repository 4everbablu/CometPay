package com.cometpay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

// engine ko frame deta hai
interface UssdFrameListener {
    fun onFrame(text: String, isMenu: Boolean, isTerminal: Boolean)
    fun onSessionEnded(reason: String)
}

// filler + repeat frame hatao
class FrameFilter {
    private var last: String? = null

    fun isSystemPlaceholder(text: String): Boolean {
        val n = text.trim().lowercase()
        return PLACEHOLDERS.any { n == it || n.startsWith(it) }
    }

    fun shouldEmit(text: String): Boolean {
        val t = text.trim()
        if (isSystemPlaceholder(t) || t == last) return false
        last = t
        return true
    }

    fun reset() { last = null }

    companion object {
        private val PLACEHOLDERS = listOf("please wait", "processing", "loading", "connecting", "ussd code running")
    }
}

// carrier dialog padhta + drive karta, overlay upar rehta
class UssdReaderService : AccessibilityService() {

    @Volatile var frameListener: UssdFrameListener? = null
    @Volatile var sessionActive: Boolean = false

    private val frameFilter = FrameFilter()
    private val main = Handler(Looper.getMainLooper())
    private val sessionEndCheck = Runnable { verifySessionGone() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 100
        }
    }

    override fun onDestroy() {
        main.removeCallbacks(sessionEndCheck)
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !sessionActive) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                main.removeCallbacks(sessionEndCheck)
                main.postDelayed(sessionEndCheck, SESSION_END_DEBOUNCE_MS)
            }
            else -> {
                val pkg = event.packageName?.toString() ?: return
                if (pkg !in USSD_PACKAGES) return
                val root = event.source ?: findUssdRoot() ?: return
                if (root.packageName?.toString() !in USSD_PACKAGES) return
                handleDialog(root)
            }
        }
    }

    override fun onInterrupt() {}

    fun sendReply(reply: String): Boolean {
        val root = findUssdRoot() ?: return false
        val edit = findFirst(root) { it.className?.contains("EditText", true) == true } ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, reply)
        }
        edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        // re-prompt same dikhta, dedup reset
        frameFilter.reset()
        val sent = findButton(root, SEND_LABELS)?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        if (!sent) edit.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return true
    }

    fun dismissDialog(): Boolean {
        val root = findUssdRoot() ?: return false
        val ok = findButton(root, DISMISS_LABELS)?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        frameFilter.reset()
        return ok
    }

    fun resetForNewSession() {
        frameFilter.reset()
        main.removeCallbacks(sessionEndCheck)
    }

    private fun handleDialog(root: AccessibilityNodeInfo) {
        val texts = mutableListOf<String>()
        walk(root) { n ->
            val cls = n.className?.toString().orEmpty()
            if (cls.contains("Button", true) || cls.contains("EditText", true)) return@walk
            n.text?.toString()?.takeIf { it.isNotBlank() && !frameFilter.isSystemPlaceholder(it) }?.let(texts::add)
        }
        val joined = texts.joinToString("\n").trim()
        if (joined.isBlank() || !frameFilter.shouldEmit(joined)) return

        val hasInput = findFirst(root) { it.className?.contains("EditText", true) == true } != null
        val isTerminal = !hasInput && hasOnlyDismiss(root)
        frameListener?.onFrame(joined, hasInput, isTerminal)
    }

    private fun findUssdRoot(): AccessibilityNodeInfo? {
        rootInActiveWindow?.let { if (it.packageName?.toString() in USSD_PACKAGES) return it }
        for (w: AccessibilityWindowInfo in windows.orEmpty()) {
            val r = w.root ?: continue
            if (r.packageName?.toString() in USSD_PACKAGES) return r
        }
        return null
    }

    private fun verifySessionGone() {
        if (!sessionActive) return
        if (findUssdRoot() == null) {
            frameFilter.reset()
            frameListener?.onSessionEnded("dialog_dismissed")
        }
    }

    private fun findButton(root: AccessibilityNodeInfo, labels: Set<String>) = findFirst(root) { n ->
        val cls = n.className?.toString().orEmpty()
        val txt = n.text?.toString()?.lowercase().orEmpty()
        cls.contains("Button", true) && labels.any { txt.contains(it) }
    }

    private fun hasOnlyDismiss(root: AccessibilityNodeInfo): Boolean {
        val labels = mutableListOf<String>()
        walk(root) { n ->
            if (n.className?.contains("Button", true) == true) n.text?.toString()?.lowercase()?.let(labels::add)
        }
        return labels.isNotEmpty() && labels.all { l -> DISMISS_LABELS.any { l.contains(it) } }
    }

    private fun findFirst(root: AccessibilityNodeInfo, pred: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (pred(root)) return root
        for (i in 0 until root.childCount) root.getChild(i)?.let { findFirst(it, pred)?.let { r -> return r } }
        return null
    }

    private fun walk(node: AccessibilityNodeInfo, visit: (AccessibilityNodeInfo) -> Unit) {
        visit(node)
        for (i in 0 until node.childCount) node.getChild(i)?.let { walk(it, visit) }
    }

    companion object {
        private const val SESSION_END_DEBOUNCE_MS = 500L

        @Volatile var instance: UssdReaderService? = null
            private set

        private val USSD_PACKAGES = setOf(
            "com.android.phone", "com.android.server.telecom",
            "com.samsung.android.app.telephonyui", "com.google.android.dialer", "com.android.dialer",
        )
        private val SEND_LABELS = setOf("send", "ok", "submit", "reply")
        private val DISMISS_LABELS = setOf("ok", "cancel", "close", "dismiss", "done")
    }
}
