package com.jakubjirak.buildhotspots.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildHotspotsSettingsTest {

    @Test
    fun `default profile directory is under the project build dir`() {
        assertEquals("/proj/build/reports/profile", BuildHotspotsSettings().getEffectiveProfileDirectory("/proj"))
    }

    @Test
    fun `override wins and blank override falls back`() {
        val settings = BuildHotspotsSettings()
        settings.profileDirectoryOverride = "/custom/profiles"
        assertEquals("/custom/profiles", settings.getEffectiveProfileDirectory("/proj"))
        settings.profileDirectoryOverride = "   "
        assertEquals("/proj/build/reports/profile", settings.getEffectiveProfileDirectory("/proj"))
    }

    @Test
    fun `defaults and loadState`() {
        val loaded = BuildHotspotsSettings().apply { slowTaskThresholdPercent = 33; maxHistoryEntries = 120 }
        val settings = BuildHotspotsSettings()
        assertEquals(BuildHotspotsSettings.DEFAULT_SLOW_TASK_THRESHOLD, settings.slowTaskThresholdPercent)
        assertEquals(BuildHotspotsSettings.DEFAULT_MAX_HISTORY, settings.maxHistoryEntries)
        settings.loadState(loaded)
        assertEquals(33, settings.slowTaskThresholdPercent)
        assertEquals(120, settings.maxHistoryEntries)
    }
}
