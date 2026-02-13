package com.jakubjirak.buildhotspots.model

/**
 * Profile of a single Gradle task execution.
 */
data class TaskProfile(
    val name: String,
    val duration: Long,
    val cacheStatus: CacheStatus = CacheStatus.NOT_CACHEABLE,
    val incremental: Boolean = true,
    val configurationTime: Long = 0,
)
