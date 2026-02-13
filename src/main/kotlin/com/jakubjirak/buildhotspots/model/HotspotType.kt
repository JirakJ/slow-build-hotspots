package com.jakubjirak.buildhotspots.model

/**
 * Classification of build performance hotspots.
 */
enum class HotspotType(val displayName: String) {
    SLOW_TASK("Slow Task"),
    CACHE_MISS("Cache Miss"),
    NON_INCREMENTAL("Non-Incremental"),
    CONFIGURATION_TIME("Configuration Time"),
    DEPENDENCY_RESOLUTION("Dependency Resolution"),
}
