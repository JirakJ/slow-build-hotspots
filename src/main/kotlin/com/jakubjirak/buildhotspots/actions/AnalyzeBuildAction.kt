package com.jakubjirak.buildhotspots.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.wm.ToolWindowManager
import com.jakubjirak.buildhotspots.analyzer.BuildHotspotAnalyzer
import com.jakubjirak.buildhotspots.history.BuildHistoryStore
import com.jakubjirak.buildhotspots.licensing.FreemiumGate
import com.jakubjirak.buildhotspots.parser.GradleProfileParser
import com.jakubjirak.buildhotspots.settings.BuildHotspotsSettings
import com.jakubjirak.buildhotspots.ui.BuildHotspotsPanel
import java.io.File

/**
 * Action that triggers analysis of the latest Gradle build profile.
 */
class AnalyzeBuildAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = BuildHotspotsSettings.getInstance()
        val projectPath = project.basePath ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Analyzing Build Profile") {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Finding latest build profile..."
                indicator.fraction = 0.1

                val profileDir = File(settings.getEffectiveProfileDirectory(projectPath))
                val parser = GradleProfileParser()
                val profileFile = parser.findLatestProfile(profileDir)

                if (profileFile == null) {
                    notify(project, "No Build Profile Found",
                        "Run your build with --profile flag: ./gradlew build --profile",
                        NotificationType.WARNING)
                    return
                }

                indicator.text = "Parsing profile: ${profileFile.name}"
                indicator.fraction = 0.4

                val profile = parser.parse(profileFile)
                if (profile == null) {
                    notify(project, "Profile Parse Error",
                        "Failed to parse build profile: ${profileFile.name}",
                        NotificationType.ERROR)
                    return
                }

                indicator.text = "Analyzing hotspots..."
                indicator.fraction = 0.7

                val analyzer = BuildHotspotAnalyzer(
                    slowTaskThresholdPercent = settings.slowTaskThresholdPercent,
                    configurationThresholdPercent = settings.configurationThresholdPercent,
                )
                val hotspots = analyzer.analyze(profile)

                // Store in history
                val freemiumGate = FreemiumGate.getInstance()
                if (freemiumGate.isPaidFeatureAvailable(FreemiumGate.FEATURE_HISTORY)) {
                    BuildHistoryStore.getInstance().addProfile(profile)
                }

                indicator.fraction = 1.0

                // Update UI on EDT
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Build Hotspots")
                    toolWindow?.show {
                        val content = toolWindow.contentManager.getContent(0)
                        val panel = content?.component as? BuildHotspotsPanel
                        panel?.updateDisplay(profile, hotspots)
                    }

                    notify(project, "Build Analysis Complete",
                        "Found ${hotspots.size} hotspot(s) in ${profile.totalTime}ms total build time",
                        NotificationType.INFORMATION)
                }

                LOG.info("Build analysis complete: ${hotspots.size} hotspots in ${profile.totalTime}ms")
            }
        })
    }

    private fun notify(
        project: com.intellij.openapi.project.Project,
        title: String,
        content: String,
        type: NotificationType,
    ) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Build Hotspots")
                .createNotification(title, content, type)
                .notify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    companion object {
        private val LOG = Logger.getInstance(AnalyzeBuildAction::class.java)
    }
}
