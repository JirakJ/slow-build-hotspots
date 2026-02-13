package com.jakubjirak.buildhotspots.analyzer

import com.jakubjirak.buildhotspots.model.BuildHotspot
import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.CacheStatus
import com.jakubjirak.buildhotspots.model.HotspotType

/**
 * Analyzes build profiles to identify performance hotspots and generate optimization suggestions.
 */
class BuildHotspotAnalyzer(
    private val slowTaskThresholdPercent: Int = DEFAULT_SLOW_TASK_THRESHOLD,
    private val configurationThresholdPercent: Int = DEFAULT_CONFIGURATION_THRESHOLD,
) {

    /**
     * Analyzes a build profile and returns a list of detected hotspots sorted by impact.
     */
    fun analyze(profile: BuildProfile): List<BuildHotspot> {
        val hotspots = mutableListOf<BuildHotspot>()

        hotspots.addAll(findSlowTasks(profile))
        hotspots.addAll(findCacheMisses(profile))
        hotspots.addAll(findNonIncrementalTasks(profile))
        hotspots.addAll(findConfigurationTimeIssues(profile))

        return hotspots.sortedByDescending { it.impactMs }
    }

    internal fun findSlowTasks(profile: BuildProfile): List<BuildHotspot> {
        if (profile.totalTime == 0L) return emptyList()

        return profile.tasks
            .filter { task ->
                val percentage = (task.duration * 100) / profile.totalTime
                percentage >= slowTaskThresholdPercent
            }
            .map { task ->
                val percentage = (task.duration * 100) / profile.totalTime
                val severity = when {
                    percentage >= 30 -> BuildHotspot.Severity.CRITICAL
                    percentage >= 20 -> BuildHotspot.Severity.WARNING
                    else -> BuildHotspot.Severity.INFO
                }
                BuildHotspot(
                    task = task.name,
                    type = HotspotType.SLOW_TASK,
                    severity = severity,
                    suggestion = "Task '${task.name}' takes ${percentage}% of build time (${task.duration}ms). " +
                        "Consider enabling build cache, incremental compilation, or splitting into smaller tasks.",
                    impactMs = task.duration,
                )
            }
    }

    internal fun findCacheMisses(profile: BuildProfile): List<BuildHotspot> {
        return profile.tasks
            .filter { it.cacheStatus == CacheStatus.MISS && it.duration > MINIMUM_IMPACT_MS }
            .map { task ->
                val severity = when {
                    task.duration >= 5000 -> BuildHotspot.Severity.CRITICAL
                    task.duration >= 2000 -> BuildHotspot.Severity.WARNING
                    else -> BuildHotspot.Severity.INFO
                }
                BuildHotspot(
                    task = task.name,
                    type = HotspotType.CACHE_MISS,
                    severity = severity,
                    suggestion = "Task '${task.name}' has a cache miss (${task.duration}ms). " +
                        "Enable the Gradle build cache with 'org.gradle.caching=true' in gradle.properties. " +
                        "Check for non-deterministic inputs (timestamps, absolute paths).",
                    impactMs = task.duration,
                )
            }
    }

    internal fun findNonIncrementalTasks(profile: BuildProfile): List<BuildHotspot> {
        return profile.tasks
            .filter { !it.incremental && it.duration > MINIMUM_IMPACT_MS }
            .map { task ->
                val severity = when {
                    task.duration >= 5000 -> BuildHotspot.Severity.CRITICAL
                    task.duration >= 2000 -> BuildHotspot.Severity.WARNING
                    else -> BuildHotspot.Severity.INFO
                }
                BuildHotspot(
                    task = task.name,
                    type = HotspotType.NON_INCREMENTAL,
                    severity = severity,
                    suggestion = "Task '${task.name}' runs non-incrementally (${task.duration}ms). " +
                        "Ensure incremental compilation is enabled and task inputs/outputs are properly declared.",
                    impactMs = task.duration,
                )
            }
    }

    internal fun findConfigurationTimeIssues(profile: BuildProfile): List<BuildHotspot> {
        val totalConfigTime = profile.tasks.sumOf { it.configurationTime }
        if (profile.totalTime == 0L || totalConfigTime == 0L) return emptyList()

        val configPercentage = (totalConfigTime * 100) / profile.totalTime
        if (configPercentage < configurationThresholdPercent) return emptyList()

        val severity = when {
            configPercentage >= 40 -> BuildHotspot.Severity.CRITICAL
            configPercentage >= 30 -> BuildHotspot.Severity.WARNING
            else -> BuildHotspot.Severity.INFO
        }

        return listOf(
            BuildHotspot(
                task = "<configuration phase>",
                type = HotspotType.CONFIGURATION_TIME,
                severity = severity,
                suggestion = "Configuration phase takes ${configPercentage}% of total build time (${totalConfigTime}ms). " +
                    "Consider using configuration cache ('org.gradle.configuration-cache=true') " +
                    "and lazy task configuration with TaskProvider.",
                impactMs = totalConfigTime,
            ),
        )
    }

    companion object {
        const val DEFAULT_SLOW_TASK_THRESHOLD = 10
        const val DEFAULT_CONFIGURATION_THRESHOLD = 20
        const val MINIMUM_IMPACT_MS = 500L
    }
}
