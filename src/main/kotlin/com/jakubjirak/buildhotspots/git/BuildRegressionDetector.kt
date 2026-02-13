package com.jakubjirak.buildhotspots.git

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jakubjirak.buildhotspots.history.BuildHistoryStore
import com.jakubjirak.buildhotspots.model.BuildProfile

/**
 * Correlates build time changes with specific commits.
 * Compares build profiles before/after each commit to detect regressions.
 */
class BuildRegressionDetector(
    private val windowSize: Int = DEFAULT_WINDOW_SIZE,
    private val regressionThresholdPercent: Double = DEFAULT_REGRESSION_THRESHOLD,
) {

    /**
     * A build regression detected by comparing build times before and after a commit.
     */
    data class BuildRegression(
        val commitHash: String,
        val commitMessage: String,
        val author: String,
        val buildTimeBefore: Long,
        val buildTimeAfter: Long,
        val deltaMs: Long,
        val percentage: Double,
    )

    /**
     * Detects build time regressions from stored build history.
     * Uses a sliding window to compute baseline and compares each build against it.
     */
    @Suppress("UNUSED_PARAMETER")
    fun detectRegressions(
        project: Project,
        buildHistory: List<BuildProfile>,
        commitMetadata: List<CommitMetadata>,
    ): List<BuildRegression> {
        if (buildHistory.size < windowSize + 1 || commitMetadata.isEmpty()) {
            return emptyList()
        }

        val regressions = mutableListOf<BuildRegression>()
        val metadataByTimestamp = commitMetadata.associateBy { it.timestamp }

        for (i in windowSize until buildHistory.size) {
            val baseline = buildHistory.subList(i - windowSize, i)
            val current = buildHistory[i]
            val avgBaseline = baseline.map { it.totalTime }.average().toLong()

            if (avgBaseline == 0L) continue

            val delta = current.totalTime - avgBaseline
            val percentage = (delta.toDouble() / avgBaseline) * PERCENTAGE_MULTIPLIER

            if (percentage >= regressionThresholdPercent) {
                val metadata = findClosestMetadata(current.timestamp, metadataByTimestamp)
                regressions.add(
                    BuildRegression(
                        commitHash = metadata?.hash ?: "unknown",
                        commitMessage = metadata?.message ?: "",
                        author = metadata?.author ?: "unknown",
                        buildTimeBefore = avgBaseline,
                        buildTimeAfter = current.totalTime,
                        deltaMs = delta,
                        percentage = percentage,
                    ),
                )
            }
        }

        LOG.info("Detected ${regressions.size} build regressions from ${buildHistory.size} builds")
        return regressions
    }

    /**
     * Detects regressions using the application-level build history store.
     */
    @Suppress("UNUSED_PARAMETER")
    fun detectRegressionsFromStore(
        project: Project,
        commitMetadata: List<CommitMetadata>,
    ): List<BuildRegression> {
        val store = BuildHistoryStore.getInstance()
        val history = store.getHistory()
        return detectRegressions(project, history, commitMetadata)
    }

    /**
     * Computes the baseline average build time from the last N builds.
     */
    fun computeBaselineAverage(buildHistory: List<BuildProfile>): Long {
        if (buildHistory.isEmpty()) return 0
        val window = buildHistory.takeLast(windowSize)
        return window.map { it.totalTime }.average().toLong()
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
        private val LOG = Logger.getInstance(BuildRegressionDetector::class.java)
        const val DEFAULT_WINDOW_SIZE = 5
        const val DEFAULT_REGRESSION_THRESHOLD = 15.0
        private const val PERCENTAGE_MULTIPLIER = 100.0
    }
}

/**
 * Metadata from a git commit associated with a build.
 */
data class CommitMetadata(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long,
)
