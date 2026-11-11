package com.jakubjirak.buildhotspots.git

import com.jakubjirak.buildhotspots.model.BuildProfile

/**
 * Build time trends mapped to git timeline.
 * Provides statistics and timeline entries for visualization.
 */
class BuildPerformanceTimeline {

    /**
     * A single entry in the build performance timeline.
     */
    data class BuildTimelineEntry(
        val commitHash: String,
        val date: Long,
        val buildTimeMs: Long,
        val taskCount: Int,
    )

    /**
     * Aggregated build statistics over a period.
     */
    data class BuildStatistics(
        val avgMs: Long,
        val medianMs: Long,
        val p95Ms: Long,
        val minMs: Long,
        val maxMs: Long,
        val totalBuilds: Int,
    )

    /**
     * Builds a timeline from build history and commit metadata.
     */
    fun buildTimeline(
        buildHistory: List<BuildProfile>,
        commitMetadata: List<CommitMetadata> = emptyList(),
    ): List<BuildTimelineEntry> {
        if (buildHistory.isEmpty()) return emptyList()

        val metadataByTimestamp = commitMetadata.associateBy { it.timestamp }

        return buildHistory.map { profile ->
            val metadata = findClosestMetadata(profile.timestamp, metadataByTimestamp)
            BuildTimelineEntry(
                commitHash = metadata?.hash ?: "unknown",
                date = profile.timestamp,
                buildTimeMs = profile.totalTime,
                taskCount = profile.tasks.size,
            )
        }.sortedBy { it.date }
    }

    /**
     * Computes build statistics from a list of build profiles.
     */
    fun computeStatistics(buildHistory: List<BuildProfile>): BuildStatistics {
        if (buildHistory.isEmpty()) {
            return BuildStatistics(
                avgMs = 0, medianMs = 0, p95Ms = 0, minMs = 0, maxMs = 0, totalBuilds = 0,
            )
        }

        val times = buildHistory.map { it.totalTime }.sorted()

        return BuildStatistics(
            avgMs = times.average().toLong(),
            medianMs = computeMedian(times),
            p95Ms = computePercentile(times, P95_PERCENTILE),
            minMs = times.first(),
            maxMs = times.last(),
            totalBuilds = times.size,
        )
    }

    private fun computeMedian(sortedValues: List<Long>): Long {
        if (sortedValues.isEmpty()) return 0
        val mid = sortedValues.size / 2
        return if (sortedValues.size % 2 == 0) {
            (sortedValues[mid - 1] + sortedValues[mid]) / 2
        } else {
            sortedValues[mid]
        }
    }

    private fun computePercentile(sortedValues: List<Long>, percentile: Double): Long {
        if (sortedValues.isEmpty()) return 0
        val index = ((percentile / PERCENTAGE_DIVISOR) * (sortedValues.size - 1)).toInt()
        return sortedValues[index.coerceIn(0, sortedValues.lastIndex)]
    }

    private fun findClosestMetadata(
        timestamp: Long,
        metadataByTimestamp: Map<Long, CommitMetadata>,
    ): CommitMetadata? {
        if (metadataByTimestamp.isEmpty()) return null
        return metadataByTimestamp.entries
            .minByOrNull { kotlin.math.abs(it.key - timestamp) }
            ?.value
    }

    companion object {
        private const val P95_PERCENTILE = 95.0
        private const val PERCENTAGE_DIVISOR = 100.0
    }
}
