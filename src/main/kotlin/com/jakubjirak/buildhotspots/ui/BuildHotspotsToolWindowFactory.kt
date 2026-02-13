package com.jakubjirak.buildhotspots.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for the Build Hotspots tool window.
 */
class BuildHotspotsToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = BuildHotspotsPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "Build Hotspots", false)
        toolWindow.contentManager.addContent(content)
    }
}
