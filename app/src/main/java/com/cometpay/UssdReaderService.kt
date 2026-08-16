package com.cometpay

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

// abhi khaali hai, sirf permission grant karne ke liye declare kiya hai
class UssdReaderService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}
