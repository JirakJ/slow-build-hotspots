package com.jakubjirak.buildhotspots.model

/**
 * A detected build performance hotspot with severity and optimization suggestion.
 */
data class BuildHotspot(
    val task: String,
    val type: HotspotType,
    val severity: Severity,
    val suggestion: String,
    val impactMs: Long,
) {
    enum class Severity(val displayName: String, val icon: String) {
        CRITICAL("Critical", "🔴"),
        WARNING("Warning", "🟡"),
        INFO("Info", "🟢"),
    }
}
