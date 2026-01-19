package com.alexjlockwood.twentyfortyeight.brightsdk

/**
 * Cross-platform interface for Bright Data SDK integration.
 * This SDK enables users to share their idle network resources in exchange for premium features.
 */
interface BrightDataSdk {
    /**
     * Check if Bright Data SDK is supported on this platform.
     * @return true if supported, false otherwise
     */
    fun isSupported(): Boolean

    /**
     * Initialize the Bright Data SDK.
     * This should be called early in the application lifecycle.
     * On first run, may show the consent dialog to the user.
     */
    fun initialize()

    /**
     * Show the consent dialog to the user.
     * Allows users to opt-in or change their choice.
     */
    fun showConsentDialog()

    /**
     * Opt out of Bright Data SDK.
     * Uninstalls the service and stops resource sharing.
     */
    fun optOut()

    /**
     * Get the current user consent choice.
     * @return ConsentChoice indicating the user's current selection
     */
    fun getConsentChoice(): ConsentChoice

    /**
     * Set a callback to be notified when user consent choice changes.
     * @param callback Function to be called when choice changes
     */
    fun setChoiceChangeCallback(callback: (ConsentChoice) -> Unit)

    /**
     * Set a callback to be notified when the consent dialog is closed.
     * @param callback Function to be called when dialog closes
     */
    fun setDialogClosedCallback(callback: () -> Unit)

    /**
     * Clean up and close the SDK.
     * Should be called when the application is shutting down.
     */
    fun close()
}

/**
 * Represents the user's consent choice for Bright Data SDK.
 */
enum class ConsentChoice {
    /** User has not made a choice yet */
    NONE,
    /** User agreed to participate (opted in) */
    OPTED_IN,
    /** User declined to participate (opted out) */
    OPTED_OUT
}

/**
 * Platform-specific factory for creating BrightDataSdk instance.
 * Implementation provided by each platform.
 */
expect fun createBrightDataSdk(): BrightDataSdk
