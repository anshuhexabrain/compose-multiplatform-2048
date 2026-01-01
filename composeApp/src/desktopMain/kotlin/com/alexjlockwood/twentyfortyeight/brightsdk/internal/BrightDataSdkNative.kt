package com.alexjlockwood.twentyfortyeight.brightsdk.internal

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.win32.StdCallLibrary

/**
 * JNA interface for Bright Data SDK native library (lum_sdk64.dll).
 * Maps C functions from lum_sdk.h to Kotlin.
 */
interface BrightDataSdkNative : StdCallLibrary {

    // Core SDK functions
    fun brd_sdk_is_supported_c(): Int
    fun brd_sdk_init_c()
    fun brd_sdk_show_consent_c()
    fun brd_sdk_opt_out_c()
    fun brd_sdk_get_consent_choice_c(): Int
    fun brd_sdk_close_c()

    // Configuration functions
    fun brd_sdk_set_appid_c(appid: String)
    fun brd_sdk_set_app_name_c(appName: String)
    fun brd_sdk_set_benefit_c(benefit: String)
    fun brd_sdk_set_skip_consent_on_init_c(skipConsent: Int)
    fun brd_sdk_set_test_mode_c(testMode: Int)

    // Callback functions
    fun brd_sdk_set_choice_change_cb_c(callback: ChoiceChangeCallback?)
    fun brd_sdk_set_on_dialog_shown_cb_c(callback: DialogShownCallback?)
    fun brd_sdk_set_on_dialog_closed_cb_c(callback: DialogClosedCallback?)

    // Service control functions (optional advanced features)
    fun brd_sdk_set_service_status_change_cb_c(callback: ServiceStatusChangeCallback?)
    fun brd_sdk_fix_service_status_c()
    fun brd_sdk_set_service_auto_start_c(enabled: Int)
    fun brd_sdk_stop_service_c()
    fun brd_sdk_start_service_c()
    fun brd_sdk_pause_c()
    fun brd_sdk_resume_c()

    // Callback interfaces
    interface ChoiceChangeCallback : Callback {
        fun invoke(choice: Int)
    }

    interface DialogShownCallback : Callback {
        fun invoke()
    }

    interface DialogClosedCallback : Callback {
        fun invoke()
    }

    interface ServiceStatusChangeCallback : Callback {
        fun invoke(status: Int)
    }

    companion object {
        // Choice constants (from lum_sdk.h)
        const val LUM_SDK_CHOICE_NONE = 0
        const val LUM_SDK_CHOICE_PEER = 1
        const val LUM_SDK_CHOICE_NOT_PEER = 2

        // Service status constants
        const val SERVICE_STATUS_NONE = 0
        const val SERVICE_STATUS_NOT_INSTALLED = 1
        const val SERVICE_STATUS_INSTALLED = 2
        const val SERVICE_STATUS_NOT_RUNNING = 3
        const val SERVICE_STATUS_RUNNING = 4
        const val SERVICE_STATUS_DISCONNECTED = 5
        const val SERVICE_STATUS_BLOCKED = 6
        const val SERVICE_STATUS_CONNECTED = 7
        const val SERVICE_STATUS_PEER = 8

        /**
         * Load the native library.
         * Tries to load from system path or bundled resources.
         */
        fun load(): BrightDataSdkNative? {
            return try {
                // Try loading lum_sdk64.dll
                Native.load("lum_sdk64", BrightDataSdkNative::class.java) as BrightDataSdkNative
            } catch (e: UnsatisfiedLinkError) {
                println("Warning: Could not load Bright Data SDK native library (lum_sdk64.dll)")
                println("Error: ${e.message}")
                println("Make sure the SDK DLLs are in the application directory or system PATH")
                null
            }
        }
    }
}
