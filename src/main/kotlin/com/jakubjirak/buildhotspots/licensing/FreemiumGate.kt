package com.jakubjirak.buildhotspots.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

/**
 * Central gate for freemium feature access.
 * Free tier: latest build analysis, basic hotspot detection.
 * Paid tier: history/trends, export, comparison, unlimited analysis.
 */
@Service(Service.Level.APP)
class FreemiumGate {

    enum class LicenseStatus {
        FREE, TRIAL, PAID, EXPIRED
    }

    /**
     * Check if a paid feature is available for the current license.
     */
    @Suppress("UNUSED_PARAMETER")
    fun isPaidFeatureAvailable(featureId: String): Boolean {
        val status = getLicenseStatus()
        return status == LicenseStatus.PAID || status == LicenseStatus.TRIAL
    }

    fun getLicenseStatus(): LicenseStatus {
        // TODO: Integrate with JetBrains Marketplace Licensing API
        return LicenseStatus.FREE
    }

    fun getMaxHistoryEntries(): Int {
        return when (getLicenseStatus()) {
            LicenseStatus.FREE -> MAX_FREE_HISTORY
            LicenseStatus.EXPIRED -> MAX_FREE_HISTORY
            LicenseStatus.TRIAL, LicenseStatus.PAID -> Int.MAX_VALUE
        }
    }

    companion object {
        const val MAX_FREE_HISTORY = 5

        const val FEATURE_HISTORY = "history"
        const val FEATURE_TRENDS = "trends"
        const val FEATURE_EXPORT = "export"
        const val FEATURE_COMPARISON = "comparison"
        const val FEATURE_SUGGESTIONS = "suggestions"

        fun getInstance(): FreemiumGate =
            ApplicationManager.getApplication().getService(FreemiumGate::class.java)
    }
}
