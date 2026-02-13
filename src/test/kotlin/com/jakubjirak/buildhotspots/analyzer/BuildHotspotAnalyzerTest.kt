package com.jakubjirak.buildhotspots.analyzer

import com.jakubjirak.buildhotspots.model.BuildHotspot
import com.jakubjirak.buildhotspots.model.BuildProfile
import com.jakubjirak.buildhotspots.model.CacheStatus
import com.jakubjirak.buildhotspots.model.HotspotType
import com.jakubjirak.buildhotspots.model.TaskProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildHotspotAnalyzerTest {

    private val analyzer = BuildHotspotAnalyzer()

    @Test
    fun `detect slow task taking more than 10 percent of build time`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 5000),
                TaskProfile(":test", 3000),
                TaskProfile(":processResources", 500),
                TaskProfile(":jar", 200),
            ),
        )

        val hotspots = analyzer.analyze(profile)

        val slowTasks = hotspots.filter { it.type == HotspotType.SLOW_TASK }
        assertTrue(slowTasks.isNotEmpty())
        assertTrue(slowTasks.any { it.task == ":compileKotlin" })
        assertTrue(slowTasks.any { it.task == ":test" })
    }

    @Test
    fun `slow task with 30 percent or more is critical`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(TaskProfile(":compileKotlin", 5000)),
        )

        val hotspots = analyzer.findSlowTasks(profile)

        assertEquals(1, hotspots.size)
        assertEquals(BuildHotspot.Severity.CRITICAL, hotspots[0].severity)
    }

    @Test
    fun `slow task with 20 percent is warning`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(TaskProfile(":test", 2500)),
        )

        val hotspots = analyzer.findSlowTasks(profile)

        assertEquals(1, hotspots.size)
        assertEquals(BuildHotspot.Severity.WARNING, hotspots[0].severity)
    }

    @Test
    fun `detect cache miss with sufficient duration`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 3000, cacheStatus = CacheStatus.MISS),
                TaskProfile(":test", 2000, cacheStatus = CacheStatus.HIT),
            ),
        )

        val hotspots = analyzer.findCacheMisses(profile)

        assertEquals(1, hotspots.size)
        assertEquals(":compileKotlin", hotspots[0].task)
        assertEquals(HotspotType.CACHE_MISS, hotspots[0].type)
    }

    @Test
    fun `cache miss below minimum impact is ignored`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":processResources", 100, cacheStatus = CacheStatus.MISS),
            ),
        )

        val hotspots = analyzer.findCacheMisses(profile)

        assertTrue(hotspots.isEmpty())
    }

    @Test
    fun `detect non-incremental tasks`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 5000, incremental = false),
                TaskProfile(":test", 3000, incremental = true),
            ),
        )

        val hotspots = analyzer.findNonIncrementalTasks(profile)

        assertEquals(1, hotspots.size)
        assertEquals(":compileKotlin", hotspots[0].task)
        assertEquals(HotspotType.NON_INCREMENTAL, hotspots[0].type)
    }

    @Test
    fun `detect configuration time issues above threshold`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 3000, configurationTime = 2500),
                TaskProfile(":test", 2000, configurationTime = 500),
            ),
        )

        val hotspots = analyzer.findConfigurationTimeIssues(profile)

        assertEquals(1, hotspots.size)
        assertEquals(HotspotType.CONFIGURATION_TIME, hotspots[0].type)
    }

    @Test
    fun `no configuration time issue when below threshold`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 3000, configurationTime = 100),
                TaskProfile(":test", 2000, configurationTime = 50),
            ),
        )

        val hotspots = analyzer.findConfigurationTimeIssues(profile)

        assertTrue(hotspots.isEmpty())
    }

    @Test
    fun `hotspots sorted by impact descending`() {
        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":small", 1500, cacheStatus = CacheStatus.MISS),
                TaskProfile(":big", 5000, cacheStatus = CacheStatus.MISS),
                TaskProfile(":medium", 3000, cacheStatus = CacheStatus.MISS),
            ),
        )

        val hotspots = analyzer.analyze(profile)

        for (i in 0 until hotspots.size - 1) {
            assertTrue(hotspots[i].impactMs >= hotspots[i + 1].impactMs)
        }
    }

    @Test
    fun `empty profile returns no hotspots`() {
        val profile = BuildProfile(totalTime = 0, tasks = emptyList())

        val hotspots = analyzer.analyze(profile)

        assertTrue(hotspots.isEmpty())
    }

    @Test
    fun `custom thresholds are respected`() {
        val customAnalyzer = BuildHotspotAnalyzer(
            slowTaskThresholdPercent = 50,
            configurationThresholdPercent = 50,
        )

        val profile = BuildProfile(
            totalTime = 10000,
            tasks = listOf(
                TaskProfile(":compileKotlin", 4000),
                TaskProfile(":test", 3000),
            ),
        )

        val hotspots = customAnalyzer.findSlowTasks(profile)

        assertTrue(hotspots.isEmpty())
    }

    @Test
    fun `cache miss severity levels`() {
        val profile = BuildProfile(
            totalTime = 50000,
            tasks = listOf(
                TaskProfile(":critical", 6000, cacheStatus = CacheStatus.MISS),
                TaskProfile(":warning", 3000, cacheStatus = CacheStatus.MISS),
                TaskProfile(":info", 800, cacheStatus = CacheStatus.MISS),
            ),
        )

        val hotspots = analyzer.findCacheMisses(profile)

        assertEquals(3, hotspots.size)
        val critical = hotspots.find { it.task == ":critical" }
        val warning = hotspots.find { it.task == ":warning" }
        val info = hotspots.find { it.task == ":info" }

        assertEquals(BuildHotspot.Severity.CRITICAL, critical?.severity)
        assertEquals(BuildHotspot.Severity.WARNING, warning?.severity)
        assertEquals(BuildHotspot.Severity.INFO, info?.severity)
    }
}
