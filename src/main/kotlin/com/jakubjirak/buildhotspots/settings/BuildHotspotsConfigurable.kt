package com.jakubjirak.buildhotspots.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class BuildHotspotsConfigurable : Configurable {

    private var enabledCheckbox: JBCheckBox? = null
    private var autoAnalyzeCheckbox: JBCheckBox? = null
    private var profileDirField: JBTextField? = null
    private var slowTaskSpinner: JSpinner? = null
    private var configThresholdSpinner: JSpinner? = null
    private var maxHistorySpinner: JSpinner? = null
    private var mainPanel: JPanel? = null

    override fun getDisplayName(): String = "Slow Build Hotspots"

    override fun createComponent(): JComponent {
        val settings = BuildHotspotsSettings.getInstance()

        enabledCheckbox = JBCheckBox("Enable Slow Build Hotspots", settings.enabled)
        autoAnalyzeCheckbox = JBCheckBox("Auto-analyze after build", settings.autoAnalyze)

        profileDirField = JBTextField(settings.profileDirectoryOverride).apply {
            emptyText.text = "build/reports/profile (default)"
        }

        slowTaskSpinner = JSpinner(SpinnerNumberModel(
            settings.slowTaskThresholdPercent, 1, 50, 1,
        ))

        configThresholdSpinner = JSpinner(SpinnerNumberModel(
            settings.configurationThresholdPercent, 5, 80, 5,
        ))

        maxHistorySpinner = JSpinner(SpinnerNumberModel(
            settings.maxHistoryEntries, 5, 500, 5,
        ))

        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox!!)
            .addComponent(autoAnalyzeCheckbox!!)
            .addLabeledComponent(JBLabel("Profile directory:"), profileDirField!!)
            .addLabeledComponent(JBLabel("Slow task threshold (%):"), slowTaskSpinner!!)
            .addLabeledComponent(JBLabel("Configuration time threshold (%):"), configThresholdSpinner!!)
            .addLabeledComponent(JBLabel("Maximum history entries:"), maxHistorySpinner!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = BuildHotspotsSettings.getInstance()
        return enabledCheckbox?.isSelected != settings.enabled ||
            autoAnalyzeCheckbox?.isSelected != settings.autoAnalyze ||
            profileDirField?.text != settings.profileDirectoryOverride ||
            slowTaskSpinner?.value != settings.slowTaskThresholdPercent ||
            configThresholdSpinner?.value != settings.configurationThresholdPercent ||
            maxHistorySpinner?.value != settings.maxHistoryEntries
    }

    override fun apply() {
        val settings = BuildHotspotsSettings.getInstance()
        settings.enabled = enabledCheckbox?.isSelected ?: true
        settings.autoAnalyze = autoAnalyzeCheckbox?.isSelected ?: false
        settings.profileDirectoryOverride = profileDirField?.text ?: ""
        settings.slowTaskThresholdPercent = slowTaskSpinner?.value as? Int ?: BuildHotspotsSettings.DEFAULT_SLOW_TASK_THRESHOLD
        settings.configurationThresholdPercent = configThresholdSpinner?.value as? Int
            ?: BuildHotspotsSettings.DEFAULT_CONFIGURATION_THRESHOLD
        settings.maxHistoryEntries = maxHistorySpinner?.value as? Int ?: BuildHotspotsSettings.DEFAULT_MAX_HISTORY
    }

    override fun reset() {
        val settings = BuildHotspotsSettings.getInstance()
        enabledCheckbox?.isSelected = settings.enabled
        autoAnalyzeCheckbox?.isSelected = settings.autoAnalyze
        profileDirField?.text = settings.profileDirectoryOverride
        slowTaskSpinner?.value = settings.slowTaskThresholdPercent
        configThresholdSpinner?.value = settings.configurationThresholdPercent
        maxHistorySpinner?.value = settings.maxHistoryEntries
    }

    override fun disposeUIResources() {
        enabledCheckbox = null
        autoAnalyzeCheckbox = null
        profileDirField = null
        slowTaskSpinner = null
        configThresholdSpinner = null
        maxHistorySpinner = null
        mainPanel = null
    }
}
