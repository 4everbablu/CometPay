package com.cometpay.ussd

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

// Full-screen opaque cover that hides the carrier UI; a watchdog guarantees it never sticks.
class OverlayController(private val context: Context) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val main = Handler(Looper.getMainLooper())

    private var root: FrameLayout? = null
    private var spinner: ProgressBar? = null
    private var resultView: TextView? = null
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var stepView: TextView? = null

    private val watchdog = Runnable { hide() }
    var onCancel: (() -> Unit)? = null

    fun canShow() = Settings.canDrawOverlays(context)

    private fun armWatchdog(delayMs: Long) {
        main.removeCallbacks(watchdog)
        main.postDelayed(watchdog, delayMs)
    }

    fun show(title: String, subtitle: String, showSpinner: Boolean = true) {
        if (!canShow()) return
        val action: () -> Unit = {
            if (root != null) update(title, subtitle, showSpinner)
            else try {
                buildView(title, subtitle, showSpinner).also { wm.addView(it, params()); root = it }
            } catch (_: Exception) { root = null }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) action() else main.post(action)
        armWatchdog(SESSION_CAP_MS)
    }

    fun update(title: String, subtitle: String, showSpinner: Boolean = true) {
        main.post {
            spinner?.visibility = if (showSpinner) View.VISIBLE else View.GONE
            resultView?.visibility = View.GONE
            titleView?.setTextColor(WHITE)
            titleView?.text = title
            subtitleView?.text = subtitle
        }
    }

    fun updateStep(label: String) {
        main.post { stepView?.text = label.uppercase() }
    }

    fun showResult(success: Boolean?, title: String, message: String) {
        armWatchdog(RESULT_CAP_MS)
        main.post {
            spinner?.visibility = View.GONE
            resultView?.visibility = View.VISIBLE
            resultView?.text = when (success) { true -> "✓"; false -> "✗"; null -> "…" }
            resultView?.setTextColor(when (success) { true -> SUCCESS; false -> DANGER; null -> TEXT_SECONDARY })
            titleView?.setTextColor(when (success) { true -> SUCCESS; false -> DANGER; null -> WHITE })
            titleView?.text = title
            subtitleView?.text = message
            stepView?.text = ""
        }
    }

    fun hide() {
        main.removeCallbacks(watchdog)
        main.post {
            root?.let { v -> try { if (v.parent != null) wm.removeViewImmediate(v) } catch (_: Exception) {} }
            root = null; spinner = null; resultView = null; titleView = null; subtitleView = null; stepView = null
        }
    }

    private fun params(): WindowManager.LayoutParams {
        // TYPE_APPLICATION_OVERLAY on API 26+, else TYPE_PHONE
        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.OPAQUE,
        ).apply { gravity = Gravity.TOP or Gravity.START }
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false, top: Int = 0, match: Boolean = false) =
        TextView(context).apply {
            this.text = text
            setTextColor(color)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                if (match) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(top) }
        }

    private fun buildView(title: String, subtitle: String, showSpinner: Boolean): FrameLayout {
        val header = label("comet pay", 22f, WHITE, bold = true)
        spinner = ProgressBar(context).apply {
            indeterminateTintList = ColorStateList.valueOf(WHITE)
            visibility = if (showSpinner) View.VISIBLE else View.GONE
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { topMargin = dp(40) }
        }
        resultView = label("✓", 46f, SUCCESS, bold = true, top = 36).apply { visibility = View.GONE }
        titleView = label(title, 20f, WHITE, bold = true, top = 28, match = true)
        subtitleView = label(subtitle, 14f, TEXT_SECONDARY, top = 8, match = true)
        stepView = label("STARTING", 11f, TEXT_SECONDARY, top = 18, match = true).apply { letterSpacing = 0.18f }
        val cancel = TextView(context).apply {
            text = "CANCEL"; setTextColor(TEXT_SECONDARY); setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            letterSpacing = 0.1f; gravity = Gravity.CENTER
            setPadding(dp(24), dp(12), dp(24), dp(12))
            background = GradientDrawable().apply { cornerRadius = dp(12).toFloat(); setStroke(dp(1), BORDER) }
            setOnClickListener { onCancel?.invoke() }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(44) }
        }
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), 0, dp(32), 0)
        }
        listOf(header, spinner, resultView, titleView, subtitleView, stepView, cancel).forEach { column.addView(it) }
        return FrameLayout(context).apply {
            setBackgroundColor(BLACK); isClickable = true
            addView(column, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER })
        }
    }

    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val SESSION_CAP_MS = 75_000L
        private const val RESULT_CAP_MS = 8_000L
        private const val BLACK = 0xFF000000.toInt()
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val TEXT_SECONDARY = 0xFF8B9099.toInt()
        private const val BORDER = 0xFF24262C.toInt()
        private const val SUCCESS = 0xFF37C463.toInt()
        private const val DANGER = 0xFFFF5A5A.toInt()
    }
}
