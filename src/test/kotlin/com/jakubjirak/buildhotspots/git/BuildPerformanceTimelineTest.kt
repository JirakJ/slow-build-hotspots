package com.jakubjirak.buildhotspots.git

import com.jakubjirak.buildhotspots.model.BuildProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildPerformanceTimelineTest {

    private val timeline = BuildPerformanceTimeline()

    @Test
    fun `compute statistics for empty history`() {
        val stats = timeline.computeStatistics(emptyList())

        assertEquals(0L, stats.avgMs)
        assertEquals(0L, stats.medianMs)
        assertEquals(0L, stats.p95Ms)
        assertEquals(0L, stats.minMs)
        assertEquals(0L, stats.maxMs)
        assertEquals(0, stats.totalBuilds)
    }

    @Test
    fun `compute statistics for single build`() {
        val history = listOf(BuildProfile(totalTime = 5000, tasks = emptyList()))

        val stats = timeline.computeStatistics(history)

        assertEquals(5000L, stats.avgMs)
        assertEquals(5000L, stats.medianMs)
        assertEquals(5000L, stats.p95Ms)
        assertEquals(5000L, stats.minMs)
        assertEquals(5000L, stats.maxMs)
        assertEquals(1, stats.totalBuilds)
    }

    @Test
    fun `compute statistics for multiple builds`() {
        val history = listOf(
            BuildProfile(totalTime = 1000, tasks = emptyList()),
            BuildProfile(totalTime = 2000, tasks = emptyList()),
            BuildProfile(totalTime = 3000, tasks = emptyList()),
            BuildProfile(totalTime = 4000, tasks = emptyList()),
            BuildProfile(totalTime = 5000, tasks = emptyList()),
        )

        val stats = timeline.computeStatistics(history)

        assertEquals(3000L, stats.avgMs)
        assertEquals(3000L, stats.medianMs)
        assertEquals(1000L, stats.minMs)
        assertEquals(5000L, stats.maxMs)
        assertEquals(5, stats.totalBuilds)
    }

    @Test
    fun `median is correct for even number of builds`() {
        val history = listOf(
            BuildProfile(totalTime = 1000, tasks = emptyList()),
            BuildProfile(totalTime = 2000, tasks = emptyList()),
            BuildProfile(totalTime = 3000, tasks = emptyList()),
            BuildProfile(totalTime = 4000, tasks = emptyList()),
        )

        val stats = timeline.computeStatistics(history)

        assertEquals(2500L, stats.medianMs)
    }

    @Test
    fun `p95 is correct for large dataset`() {
        val history = (1..100).map {
            BuildProfile(totalTime = it * 100L, tasks = emptyList())
        }

        val stats = timeline.computeStatistics(history)

        assertEquals(100, stats.totalBuilds)
        assertEquals(100L, stats.minMs)
        assertEquals(10000L, stats.maxMs)
        // p95 should be around 9500
        assert(stats.p95Ms >= 9400L) { "p95 should be >= 9400, was ${stats.p95Ms}" }
    }

    @Test
    fun `statistics reflect correct average`() {
        val history = listOf(
            BuildProfile(totalTime = 10000, tasks = emptyList()),
            BuildProfile(totalTime = 20000, tasks = emptyList()),
            BuildProfile(totalTime = 30000, tasks = emptyList()),
        )

        val stats = timeline.computeStatistics(history)

        assertEquals(20000L, stats.avgMs)
    }

    @Test
    fun `timeline is sorted by date and attaches closest commit`() {
        val history = listOf(
            BuildProfile(totalTime = 300, tasks = emptyList(), timestamp = 30),
            BuildProfile(totalTime = 100, tasks = emptyList(), timestamp = 10),
        )
        val metadata = listOf(CommitMetadata("c1", "m", "a", 9), CommitMetadata("c3", "m", "a", 31))

        val entries = timeline.buildTimeline(history, metadata)

        assertEquals(listOf(10L, 30L), entries.map { it.date })
        assertEquals(listOf("c1", "c3"), entries.map { it.commitHash })
        assertEquals(listOf(100L, 300L), entries.map { it.buildTimeMs })
    }

    @Test
    fun `timeline without metadata uses unknown hash`() {
        val entries = timeline.buildTimeline(listOf(BuildProfile(totalTime = 1, tasks = emptyList(), timestamp = 1)))
        assertEquals("unknown", entries.single().commitHash)
        assertTrue(timeline.buildTimeline(emptyList()).isEmpty())
    }
}
