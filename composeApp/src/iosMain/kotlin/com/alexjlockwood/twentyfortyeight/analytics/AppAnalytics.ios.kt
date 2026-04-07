package com.alexjlockwood.twentyfortyeight.analytics

import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk

actual fun createAppAnalytics(brightDataSdk: BrightDataSdk): AppAnalytics = NoOpAppAnalytics

private object NoOpAppAnalytics : AppAnalytics {
    override fun trackAppStart(brdEnabled: Boolean) = Unit
    override fun trackBrdState(brdEnabled: Boolean) = Unit
}
