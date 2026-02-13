package com.jakubjirak.buildhotspots.git

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jakubjirak.buildhotspots.history.BuildHistoryStore
import com.jakubjirak.buildhotspots.model.BuildProfile

/**
 * Estimates build time impact of changes in a PR/branch.
 * Based on historical data: which files/modules affect build time most.
 */
class BuildImpactPredictor {

    /**
     * Predicted build impact for a set of changed files.
     */
    data class BuildImpact(
        val changedFiles: List<String>,
        val estimatedImpactMs: Long,
        val confidence: Double,
        val affectedTasks: List<String>,
    )

    /**
     * Predicts build time impact based on changed files and historical build data.
     */
    @Suppress("UNUSED_PARAMETER")
    fun predictImpact(
        project: Project,
        changedFiles: List<String>,
        buildHistory: List<BuildProfile>,
    ): BuildImpact {
        if (changedFiles.isEmpty() || buildHistory.isEmpty()) {
            return BuildImpact(
                changedFiles = changedFiles,
                estimatedImpactMs = 0,
                confidence = 0.0,
                affectedTasks = emptyList(),
            )
        }

        val affectedTasks = mapFilesToTasks(changedFiles)
        val avgBuildTime = buildHistory.map { it.totalTime }.average().toLong()
        val taskImpact = estimateTaskImpact(affectedTasks, buildHistory)
        val confidence = calculateConfidence(buildHistory.size, affectedTasks.size)

        LOG.info(
            "Predicted impact for ${changedFiles.size} files: " +
                "${taskImpact}ms, confidence: $confidence",
        )

        return BuildImpact(
            changedFiles = changedFiles,
            estimatedImpactMs = taskImpact,
            confidence = confidence,
            affectedTasks = affectedTasks,
        )
    }

    /**
     * Predicts impact using the stored build history.
     */
    @Suppress("UNUSED_PARAMETER")
    fun predictImpactFromStore(
        project: Project,
        changedFiles: List<String>,
    ): BuildImpact {
        val store = BuildHistoryStore.getInstance()
        val history = store.getHistory()
        return predictImpact(project, changedFiles, history)
    }

    /**
     * Maps changed files to likely affected Gradle tasks based on path heuristics.
     */
    internal fun mapFilesToTasks(changedFiles: List<String>): List<String> {
        val tasks = mutableSetOf<String>()

        for (file in changedFiles) {
            val task = inferTaskFromPath(file)
            if (task != null) {
                tasks.add(task)
            }
        }

        return tasks.toList()
    }

    private fun inferTaskFromPath(filePath: String): String? {
        val parts = filePath.split("/")
        val srcIndex = parts.indexOf("src")
        if (srcIndex < 0) return null

        val moduleParts = parts.subList(0, srcIndex)
        val modulePrefix = if (moduleParts.isNotEmpty()) {
            ":" + moduleParts.joinToString(":")
        } else {
            ""
        }

        return when {
            filePath.endsWith(".kt") || filePath.endsWith(".java") ->
                "${modulePrefix}:compileKotlin"
            filePath.endsWith(".xml") || filePath.endsWith(".properties") ->
                "${modulePrefix}:processResources"
            else -> "${modulePrefix}:build"
        }
    }

    private fun estimateTaskImpact(
        affectedTasks: List<String>,
        buildHistory: List<BuildProfile>,
    ): Long {
        if (affectedTasks.isEmpty() || buildHistory.isEmpty()) return 0

        var totalImpact = 0L
        for (taskName in affectedTasks) {
            val avgDuration = buildHistory
                .flatMap { it.tasks }
                .filter { it.name.endsWith(taskName.substringAfterLast(":")) }
                .map { it.duration }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toLong() ?: 0

            totalImpact += avgDuration
        }
        return totalImpact
    }

    private fun calculateConfidence(historySize: Int, taskCount: Int): Double {
        val historyFactor = (historySize.toDouble() / OPTIMAL_HISTORY_SIZE).coerceAtMost(1.0)
        val taskFactor = if (taskCount > 0) TASK_CONFIDENCE_BASE else 0.0
        return ((historyFactor * HISTORY_WEIGHT + taskFactor * TASK_WEIGHT)).coerceIn(0.0, 1.0)
    }

    companion object {
        private val LOG = Logger.getInstance(BuildImpactPredictor::class.java)
        private const val OPTIMAL_HISTORY_SIZE = 20
        private const val HISTORY_WEIGHT = 0.7
        private const val TASK_WEIGHT = 0.3
        private const val TASK_CONFIDENCE_BASE = 1.0
    }
}
