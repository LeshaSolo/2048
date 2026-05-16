package com.example.noblenumbers.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface RewardedAdsManager {
    val isRewardedAdAvailable: StateFlow<Boolean>
    fun loadRewardedAd()
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onUnavailable: () -> Unit)
}
