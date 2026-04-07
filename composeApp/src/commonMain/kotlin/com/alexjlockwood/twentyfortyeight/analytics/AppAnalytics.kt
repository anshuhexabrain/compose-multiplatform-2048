package com.alexjlockwood.twentyfortyeight.analytics

import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk

interface AppAnalytics {
    fun trackAppStart(brdEnabled: Boolean)
    fun trackBrdState(brdEnabled: Boolean)
}

expect fun createAppAnalytics(brightDataSdk: BrightDataSdk): AppAnalytics
