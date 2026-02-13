package com.jakubjirak.buildhotspots.model

/**
 * Cache status for a Gradle task execution.
 */
enum class CacheStatus(val displayName: String) {
    HIT("Cache Hit"),
    MISS("Cache Miss"),
    NO_SOURCE("No Source"),
    DISABLED("Disabled"),
    NOT_CACHEABLE("Not Cacheable"),
}
