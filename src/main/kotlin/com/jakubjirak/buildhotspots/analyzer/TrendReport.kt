/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package com.jakubjirak.buildhotspots.analyzer

import com.jakubjirak.buildhotspots.git.BuildPerformanceTimeline
import com.jakubjirak.buildhotspots.git.BuildRegressionDetector
import com.jakubjirak.buildhotspots.model.BuildProfile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Renders the plain-text "Trend" tab: a bar chart of the current build's slowest tasks followed by
 * statistics and regressions over the stored build history.
 */
object TrendReport {

    private const val BAR_WIDTH = 40
    private const val NAME_WIDTH = 40
    private const val TOP_TASKS = 15
    private const val PERCENT = 100
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun render(
        profile: BuildProfile,
        history: List<BuildProfile>,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String = buildString {
        appendBreakdown(profile)
        appendLine()
        appendHistory(history, zone)
    }

    private fun StringBuilder.appendBreakdown(profile: BuildProfile) {
        appendLine("=== Build Time Breakdown ===")
        appendLine()
        appendLine("Total build time: ${profile.totalTime}ms")
        appendLine("Tasks executed: ${profile.tasks.size}")
        appendLine()

        val maxDuration = profile.tasks.maxOfOrNull { it.duration }?.coerceAtLeast(1) ?: 1
        profile.tasks.sortedByDescending { it.duration }.take(TOP_TASKS).forEach { task ->
            val barLength = (task.duration.toDouble() / maxDuration * BAR_WIDTH).toInt().coerceAtLeast(1)
            val percentage = if (profile.totalTime > 0) task.duration * PERCENT / profile.totalTime else 0
            val name = task.name.padEnd(NAME_WIDTH).take(NAME_WIDTH)
            appendLine("$name ${"█".repeat(barLength)} ${task.duration}ms ($percentage%)")
        }
    }

    private fun StringBuilder.appendHistory(history: List<BuildProfile>, zone: ZoneId) {
        appendLine("=== Build History ===")
        appendLine()
        if (history.isEmpty()) {
            appendLine("No builds stored yet.")
            return
        }
        val stats = BuildPerformanceTimeline().computeStatistics(history)
        appendLine("Builds analyzed: ${stats.totalBuilds}")
        appendLine("Average: ${stats.avgMs}ms   Median: ${stats.medianMs}ms   P95: ${stats.p95Ms}ms")
        appendLine("Fastest: ${stats.minMs}ms   Slowest: ${stats.maxMs}ms")
        appendLine()

        val window = BuildRegressionDetector.DEFAULT_WINDOW_SIZE
        val rule = "≥${BuildRegressionDetector.DEFAULT_REGRESSION_THRESHOLD.toInt()}% slower than the previous $window builds"
        val regressions = BuildRegressionDetector().detectRegressions(history.sortedBy { it.timestamp })
        if (history.size <= window) {
            appendLine("Regression detection needs more than $window analyzed builds (have ${history.size}).")
        } else if (regressions.isEmpty()) {
            appendLine("No regressions ($rule).")
        } else {
            appendLine("Regressions ($rule):")
            regressions.forEach { r ->
                val date = DATE_FORMAT.format(Instant.ofEpochMilli(r.buildTimestamp).atZone(zone))
                appendLine("  $date  ${r.buildTimeBefore}ms → ${r.buildTimeAfter}ms (+${r.percentage.toInt()}%)")
            }
        }
    }
}
