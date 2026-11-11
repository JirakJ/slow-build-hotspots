package com.jakubjirak.buildhotspots.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*

class BuildHotspotsConfigurable : BoundConfigurable("Slow Build Hotspots") {

    private val settings get() = BuildHotspotsSettings.getInstance()

    override fun createPanel() = panel {
        group("Thresholds") {
            row("Slow task threshold (%):") {
                spinner(1..100)
                    .bindIntValue(settings::slowTaskThresholdPercent)
                    .comment("Tasks taking more than this percentage of total build time are flagged")
            }
            row("Configuration threshold (%):") {
                spinner(1..100)
                    .bindIntValue(settings::configurationThresholdPercent)
                    .comment("Configuration phases taking more than this percentage are flagged")
            }
        }
        group("Storage") {
            row("Profile directory:") {
                cell(TextFieldWithBrowseButton().apply {
                    addBrowseFolderListener(
                        com.intellij.openapi.ui.TextBrowseFolderListener(
                            FileChooserDescriptorFactory.createSingleFolderDescriptor()
                                .withTitle("Select Profile Directory"), null))
                })
                    .bindText(settings::profileDirectoryOverride)
                    .comment("Override Gradle profile directory. Leave empty for auto-detect")
            }
            row("Max history entries:") {
                spinner(10..500)
                    .bindIntValue(settings::maxHistoryEntries)
                    .comment("Maximum number of analyzed builds kept for the Trend tab statistics")
            }
        }
    }
}
