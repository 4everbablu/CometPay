package com.cometpay.app.presentation.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

fun canLock(activity: FragmentActivity) =
    BiometricManager.from(activity).canAuthenticate(ALLOWED) == BiometricManager.BIOMETRIC_SUCCESS

fun promptUnlock(activity: FragmentActivity, onOk: () -> Unit) {
    val cb = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onOk()
    }
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Comet Pay")
        .setSubtitle("Verify it's you to continue")
        .setAllowedAuthenticators(ALLOWED)
        .build()
    BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), cb).authenticate(info)
}
