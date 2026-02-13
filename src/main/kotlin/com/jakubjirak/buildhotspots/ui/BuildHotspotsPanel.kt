package com.jakubjirak.buildhotspots.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.jakubjirak.buildhotspots.analyzer.BuildHotspotAnalyzer
import com.jakubjirak.buildhotspots.model.BuildHotspot
import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.parser.GradleProfileParser
import com.jakubjirak.buildhotspots.settings.BuildHotspotsSettings
import java.awt.BorderLayout
import java.io.File
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.table.DefaultTableModel

/**
 * Main panel for the Build Hotspots tool window with task breakdown, hotspots, and trend tabs.
 */
class BuildHotspotsPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val taskTableModel = DefaultTableModel(arrayOf("Task", "Duration (ms)", "Cache", "Incremental"), 0)
    private val hotspotTableModel = DefaultTableModel(arrayOf("Severity", "Type", "Task", "Impact (ms)", "Suggestion"), 0)
    private val trendArea = JTextArea().apply {
        isEditable = false
        font = JBUI.Fonts.create("Monospaced", 12)
    }
    private val statusLabel = JBLabel("No build profile analyzed yet. Click 'Analyze' to start.")

    init {
        val tabbedPane = JTabbedPane()

        tabbedPane.addTab("Task Breakdown", createTaskBreakdownTab())
        tabbedPane.addTab("Hotspots", createHotspotsTab())
        tabbedPane.addTab("Trend", createTrendTab())

        val toolbar = createToolbar()

        add(toolbar, BorderLayout.NORTH)
        add(tabbedPane, BorderLayout.CENTER)
        add(statusLabel, BorderLayout.SOUTH)
    }

    private fun createToolbar(): JPanel {
        val toolbar = JPanel().apply {
            layout = java.awt.FlowLayout(java.awt.FlowLayout.LEFT)
        }

        val analyzeButton = JButton("Analyze Latest Build").apply {
            addActionListener { analyzeLatestBuild() }
        }
        toolbar.add(analyzeButton)

        val clearButton = JButton("Clear").apply {
            addActionListener { clearResults() }
        }
        toolbar.add(clearButton)

        return toolbar
    }

    private fun createTaskBreakdownTab(): JPanel {
        val panel = JPanel(BorderLayout())
        val table = JBTable(taskTableModel).apply {
            autoCreateRowSorter = true
        }
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        return panel
    }

    private fun createHotspotsTab(): JPanel {
        val panel = JPanel(BorderLayout())
        val table = JBTable(hotspotTableModel).apply {
            autoCreateRowSorter = true
        }
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        return panel
    }

    private fun createTrendTab(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(JBScrollPane(trendArea), BorderLayout.CENTER)
        return panel
    }

    fun analyzeLatestBuild() {
        val settings = BuildHotspotsSettings.getInstance()
        val projectPath = project.basePath ?: return
        val profileDir = File(settings.getEffectiveProfileDirectory(projectPath))

        val parser = GradleProfileParser()
        val profileFile = parser.findLatestProfile(profileDir)

        if (profileFile == null) {
            statusLabel.text = "No profile found. Run: ./gradlew build --profile"
            return
        }

        val profile = parser.parse(profileFile)
        if (profile == null) {
            statusLabel.text = "Failed to parse profile: ${profileFile.name}"
            return
        }

        val analyzer = BuildHotspotAnalyzer(
            slowTaskThresholdPercent = settings.slowTaskThresholdPercent,
            configurationThresholdPercent = settings.configurationThresholdPercent,
        )
        val hotspots = analyzer.analyze(profile)

        updateDisplay(profile, hotspots)
        LOG.info("Build analysis complete: ${hotspots.size} hotspots found")
    }

    fun updateDisplay(profile: BuildProfile, hotspots: List<BuildHotspot>) {
        updateTaskBreakdown(profile)
        updateHotspots(hotspots)
        updateTrend(profile)
        statusLabel.text = "Analyzed: ${profile.tasks.size} tasks, ${hotspots.size} hotspots, " +
            "total build time: ${profile.totalTime}ms"
    }

    private fun updateTaskBreakdown(profile: BuildProfile) {
        taskTableModel.rowCount = 0
        profile.tasks.sortedByDescending { it.duration }.forEach { task ->
            taskTableModel.addRow(
                arrayOf<Any>(
                    task.name,
                    task.duration,
                    task.cacheStatus.displayName,
                    if (task.incremental) "Yes" else "No",
                ),
            )
        }
    }

    private fun updateHotspots(hotspots: List<BuildHotspot>) {
        hotspotTableModel.rowCount = 0
        hotspots.forEach { hotspot ->
            hotspotTableModel.addRow(
                arrayOf<Any>(
                    "${hotspot.severity.icon} ${hotspot.severity.displayName}",
                    hotspot.type.displayName,
                    hotspot.task,
                    hotspot.impactMs,
                    hotspot.suggestion,
                ),
            )
        }
    }

    private fun updateTrend(profile: BuildProfile) {
        val sb = StringBuilder()
        sb.appendLine("=== Build Time Breakdown ===")
        sb.appendLine()
        sb.appendLine("Total build time: ${profile.totalTime}ms")
        sb.appendLine("Tasks executed: ${profile.tasks.size}")
        sb.appendLine()

        val maxDuration = profile.tasks.maxOfOrNull { it.duration } ?: 1
        val barWidth = 40

        profile.tasks.sortedByDescending { it.duration }.take(15).forEach { task ->
            val barLength = ((task.duration.toDouble() / maxDuration) * barWidth).toInt().coerceAtLeast(1)
            val bar = "█".repeat(barLength)
            val percentage = if (profile.totalTime > 0) {
                (task.duration * 100) / profile.totalTime
            } else {
                0
            }
            sb.appendLine("${task.name.padEnd(40).take(40)} $bar ${task.duration}ms (${percentage}%)")
        }

        trendArea.text = sb.toString()
    }

    private fun clearResults() {
        taskTableModel.rowCount = 0
        hotspotTableModel.rowCount = 0
        trendArea.text = ""
        statusLabel.text = "Results cleared."
    }

    companion object {
        private val LOG = Logger.getInstance(BuildHotspotsPanel::class.java)
    }
}
