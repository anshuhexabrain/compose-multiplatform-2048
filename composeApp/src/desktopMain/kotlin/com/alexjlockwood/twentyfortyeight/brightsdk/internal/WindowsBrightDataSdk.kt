package com.alexjlockwood.twentyfortyeight.brightsdk.internal

import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import com.alexjlockwood.twentyfortyeight.brightsdk.ConsentChoice

/**
 * Windows implementation of Bright Data SDK using JNA to call native DLL functions.
 */
class WindowsBrightDataSdk : BrightDataSdk {

    private val nativeLib: BrightDataSdkNative?
    private var choiceChangeCallback: ((ConsentChoice) -> Unit)? = null
    private var isInitialized = false

    init {
        // Extract SDK resources (DLLs) from JAR to filesystem
        SdkResourceLoader.extractSdkResources()
        // Now load the native library
        nativeLib = BrightDataSdkNative.load()
    }

    // JNA callbacks that bridge to Kotlin callbacks
    private val nativeChoiceChangeCallback = object : BrightDataSdkNative.ChoiceChangeCallback {
        override fun invoke(choice: Int) {
            val consentChoice = mapChoiceToEnum(choice)
            choiceChangeCallback?.invoke(consentChoice)
        }
    }

    private val dialogShownCallback = object : BrightDataSdkNative.DialogShownCallback {
        override fun invoke() {
            println("Bright Data: Consent dialog shown")
        }
    }

    private val dialogClosedCallback = object : BrightDataSdkNative.DialogClosedCallback {
        override fun invoke() {
            println("Bright Data: Consent dialog closed")
        }
    }

    override fun isSupported(): Boolean {
        if (nativeLib == null) {
            return false
        }
        return try {
            nativeLib.brd_sdk_is_supported_c() != 0
        } catch (e: Exception) {
            println("Bright Data: Error checking support: ${e.message}")
            false
        }
    }

    override fun initialize() {
        if (nativeLib == null) {
            println("Bright Data: SDK not available (library not loaded)")
            return
        }

        if (isInitialized) {
            println("Bright Data: SDK already initialized")
            return
        }

        try {
            println("Bright Data: Initializing SDK...")

            // Set app configuration
            // TODO: Replace with your actual app_id from Bright Data when you receive it
            nativeLib.brd_sdk_set_appid_c("win_hexabrain_systems_pvt_ltd.2048_game")
            nativeLib.brd_sdk_set_app_name_c("2048 Game")
            nativeLib.brd_sdk_set_benefit_c("Play unlimited games for free")

            // Set test mode to false for production
            // Set to true (1) for testing without real app_id
            nativeLib.brd_sdk_set_test_mode_c(0)

            // Don't skip consent on first run - let SDK show dialog automatically
            nativeLib.brd_sdk_set_skip_consent_on_init_c(0)

            // Set up callbacks
            nativeLib.brd_sdk_set_choice_change_cb_c(nativeChoiceChangeCallback)
            nativeLib.brd_sdk_set_on_dialog_shown_cb_c(dialogShownCallback)
            nativeLib.brd_sdk_set_on_dialog_closed_cb_c(dialogClosedCallback)

            // Initialize the SDK
            nativeLib.brd_sdk_init_c()

            isInitialized = true
            println("Bright Data: SDK initialized successfully")
            println("Bright Data: Current choice = ${getConsentChoice()}")

        } catch (e: Exception) {
            println("Bright Data: Error during initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun showConsentDialog() {
        if (nativeLib == null) {
            println("Bright Data: Cannot show dialog - SDK not available")
            return
        }

        try {
            println("Bright Data: Showing consent dialog...")
            nativeLib.brd_sdk_show_consent_c()
        } catch (e: Exception) {
            println("Bright Data: Error showing consent dialog: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun optOut() {
        if (nativeLib == null) {
            println("Bright Data: Cannot opt out - SDK not available")
            return
        }

        try {
            println("Bright Data: Opting out...")
            nativeLib.brd_sdk_opt_out_c()
            println("Bright Data: Opted out successfully")
        } catch (e: Exception) {
            println("Bright Data: Error during opt-out: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getConsentChoice(): ConsentChoice {
        if (nativeLib == null) {
            return ConsentChoice.NONE
        }

        return try {
            val choice = nativeLib.brd_sdk_get_consent_choice_c()
            mapChoiceToEnum(choice)
        } catch (e: Exception) {
            println("Bright Data: Error getting choice: ${e.message}")
            ConsentChoice.NONE
        }
    }

    override fun setChoiceChangeCallback(callback: (ConsentChoice) -> Unit) {
        choiceChangeCallback = callback
        println("Bright Data: Choice change callback registered")
    }

    override fun close() {
        if (nativeLib == null || !isInitialized) {
            return
        }

        try {
            println("Bright Data: Closing SDK...")
            nativeLib.brd_sdk_close_c()
            isInitialized = false
            println("Bright Data: SDK closed")
        } catch (e: Exception) {
            println("Bright Data: Error during close: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Map native choice integer to ConsentChoice enum.
     */
    private fun mapChoiceToEnum(choice: Int): ConsentChoice {
        return when (choice) {
            BrightDataSdkNative.LUM_SDK_CHOICE_NONE -> ConsentChoice.NONE
            BrightDataSdkNative.LUM_SDK_CHOICE_PEER -> ConsentChoice.OPTED_IN
            BrightDataSdkNative.LUM_SDK_CHOICE_NOT_PEER -> ConsentChoice.OPTED_OUT
            else -> {
                println("Bright Data: Unknown choice value: $choice")
                ConsentChoice.NONE
            }
        }
    }
}
