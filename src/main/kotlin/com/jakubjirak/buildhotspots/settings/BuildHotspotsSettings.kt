package com.jakubjirak.buildhotspots.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "BuildHotspotsSettings",
    storages = [Storage(value = "buildHotspotsSettings.xml", roamingType = RoamingType.DEFAULT)],
)
class BuildHotspotsSettings : PersistentStateComponent<BuildHotspotsSettings> {

    var enabled: Boolean = true
    var slowTaskThresholdPercent: Int = DEFAULT_SLOW_TASK_THRESHOLD
    var configurationThresholdPercent: Int = DEFAULT_CONFIGURATION_THRESHOLD
    var profileDirectoryOverride: String = ""
    var autoAnalyze: Boolean = false
    var maxHistoryEntries: Int = DEFAULT_MAX_HISTORY

    override fun getState(): BuildHotspotsSettings = this

    override fun loadState(state: BuildHotspotsSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun getEffectiveProfileDirectory(projectPath: String): String {
        return profileDirectoryOverride.ifBlank { "$projectPath/build/reports/profile" }
    }

    companion object {
        const val DEFAULT_SLOW_TASK_THRESHOLD = 10
        const val DEFAULT_CONFIGURATION_THRESHOLD = 20
        const val DEFAULT_MAX_HISTORY = 50

        fun getInstance(): BuildHotspotsSettings =
            ApplicationManager.getApplication().getService(BuildHotspotsSettings::class.java)
    }
}
