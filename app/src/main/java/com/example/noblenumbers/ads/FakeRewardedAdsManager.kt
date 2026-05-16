package com.example.noblenumbers.ads

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRewardedAdsManager : RewardedAdsManager {
    private val available = MutableStateFlow(true)

    override val isRewardedAdAvailable: StateFlow<Boolean> = available

    override fun loadRewardedAd() {
        available.value = true
    }

    override fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        if (available.value) {
            onRewardEarned()
            available.value = true
        } else {
            onUnavailable()
        }
    }
}
