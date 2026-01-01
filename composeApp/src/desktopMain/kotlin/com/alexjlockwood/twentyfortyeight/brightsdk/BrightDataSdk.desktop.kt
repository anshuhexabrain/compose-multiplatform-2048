package com.alexjlockwood.twentyfortyeight.brightsdk

import com.alexjlockwood.twentyfortyeight.brightsdk.internal.WindowsBrightDataSdk

/**
 * Desktop (Windows) implementation factory for Bright Data SDK.
 * Returns Windows-specific implementation on Windows, or a no-op implementation on other platforms.
 */
actual fun createBrightDataSdk(): BrightDataSdk {
    val osName = System.getProperty("os.name").lowercase()
    return if (osName.contains("windows")) {
        WindowsBrightDataSdk()
    } else {
        NoOpBrightDataSdk()
    }
}

/**
 * No-operation implementation for non-Windows platforms (Mac, Linux).
 */
private class NoOpBrightDataSdk : BrightDataSdk {
    override fun isSupported(): Boolean = false
    override fun initialize() {}
    override fun showConsentDialog() {}
    override fun optOut() {}
    override fun getConsentChoice(): ConsentChoice = ConsentChoice.NONE
    override fun setChoiceChangeCallback(callback: (ConsentChoice) -> Unit) {}
    override fun close() {}
}
