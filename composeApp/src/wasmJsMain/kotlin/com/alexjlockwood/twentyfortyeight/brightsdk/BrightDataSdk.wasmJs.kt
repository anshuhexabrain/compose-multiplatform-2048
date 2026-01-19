package com.alexjlockwood.twentyfortyeight.brightsdk

/**
 * Wasm/JS implementation factory for Bright Data SDK.
 * Returns no-op implementation as this integration is Windows-specific.
 */
actual fun createBrightDataSdk(): BrightDataSdk {
    return NoOpBrightDataSdk()
}

private class NoOpBrightDataSdk : BrightDataSdk {
    override fun isSupported(): Boolean = false
    override fun initialize() {}
    override fun showConsentDialog() {}
    override fun optOut() {}
    override fun getConsentChoice(): ConsentChoice = ConsentChoice.NONE
    override fun setChoiceChangeCallback(callback: (ConsentChoice) -> Unit) {}
    override fun setDialogClosedCallback(callback: () -> Unit) {}
    override fun close() {}
}
