package com.jakubjirak.buildhotspots.model

/**
 * Complete profile of a Gradle build execution.
 */
data class BuildProfile(
    val totalTime: Long,
    val tasks: List<TaskProfile>,
    val timestamp: Long = System.currentTimeMillis(),
    val projectPath: String = "",
)
