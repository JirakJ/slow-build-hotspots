package com.jakubjirak.buildhotspots.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.jakubjirak.buildhotspots.analyzer.BuildHotspotAnalyzer
import com.jakubjirak.buildhotspots.analyzer.TrendReport
import com.jakubjirak.buildhotspots.history.BuildHistoryStore
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
        BuildHistoryStore.getInstance().addProfile(profile, settings.maxHistoryEntries)

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
        trendArea.text = TrendReport.render(profile, BuildHistoryStore.getInstance().getHistory())
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
