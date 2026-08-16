package com.cometpay.app.presentation.screens

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Txn(val title: String, val sub: String, val amount: String, val ok: Boolean?)

// SharedPreferences-backed settings + history
object Store {
    private const val REC = "\n"
    private const val FLD = "\t"

    private fun p(c: Context) = c.getSharedPreferences("comet", Context.MODE_PRIVATE)
    private fun put(c: Context, block: android.content.SharedPreferences.Editor.() -> Unit) =
        p(c).edit().apply(block).apply()

    fun screenLock(c: Context) = p(c).getBoolean("lock", false)
    fun setScreenLock(c: Context, on: Boolean) = put(c) { putBoolean("lock", on) }

    fun onboarded(c: Context) = p(c).getBoolean("onboarded", false)
    fun setOnboarded(c: Context) = put(c) { putBoolean("onboarded", true) }

    fun bank(c: Context) = p(c).getInt("bank", 0)
    fun setBank(c: Context, v: Int) = put(c) { putInt("bank", v) }
    fun sim(c: Context) = p(c).getInt("sim", 0)
    fun setSim(c: Context, v: Int) = put(c) { putInt("sim", v) }
    fun otherBank(c: Context) = p(c).getString("otherBank", "").orEmpty()
    fun setOtherBank(c: Context, v: String) = put(c) { putString("otherBank", v) }

    fun recipients(c: Context): List<Pair<String, String>> =
        rows(c, "recips").mapNotNull { val f = it.split(FLD); if (f.size == 2) f[0] to f[1] else null }

    fun addRecipient(c: Context, name: String, vpa: String) =
        save(c, "recips", (recipients(c).filterNot { it.second == vpa } + (name to vpa)).map { it.first + FLD + it.second })

    fun removeRecipient(c: Context, vpa: String) =
        save(c, "recips", recipients(c).filterNot { it.second == vpa }.map { it.first + FLD + it.second })

    fun history(c: Context): List<Txn> =
        rows(c, "txns").mapNotNull { val f = it.split(FLD); if (f.size == 4) Txn(f[0], f[1], f[2], f[3].toBooleanStrictOrNull()) else null }

    // newest first, cap 20
    fun addTxn(c: Context, title: String, amount: String, ok: Boolean?) {
        val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        val all = (listOf(Txn(title, time, amount, ok)) + history(c)).take(20)
        save(c, "txns", all.map { it.title + FLD + it.sub + FLD + it.amount + FLD + it.ok })
    }

    private fun rows(c: Context, key: String) =
        p(c).getString(key, "").orEmpty().split(REC).filter { it.isNotBlank() }

    private fun save(c: Context, key: String, rows: List<String>) =
        put(c) { putString(key, rows.joinToString(REC)) }
}
